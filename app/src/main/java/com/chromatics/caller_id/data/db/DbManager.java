package com.chromatics.caller_id.data.db;

import com.chromatics.caller_id.Storage;
import com.chromatics.caller_id.common.CommunityDatabase;
import com.chromatics.caller_id.common.NumberFilter;
import com.chromatics.caller_id.models.CommunityDatabaseDataSlice;
import com.chromatics.caller_id.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class DbManager {

    public static class UpdateResult {
        public enum Status {
            UPDATED, NO_UPDATE, OUTDATED_APP, BAD_SECONDARY, UNEXPECTED_RESPONSE, DOWNLOAD_ERROR, UNKNOWN_ERROR
        }

        private final boolean updated;
        private final Status status;

        UpdateResult(boolean updated, Status status) {
            this.updated = updated;
            this.status = status;
        }

        public boolean isUpdated() {
            return updated;
        }

        public Status getStatus() {
            return status;
        }
    }

    public static final String DEFAULT_URL = "https://gitlab.com/xynngh/YetAnotherCallBlocker_data/raw/zip_v1/archives/sia.zip";

    private static final Logger LOG = LoggerFactory.getLogger(DbManager.class);

    private final Storage storage;
    private final String pathPrefix;
    private final DbDownloader dbDownloader;
    private final DbUpdateRequester dbUpdateRequester;
    private final CommunityDatabase communityDatabase;
    private NumberFilter numberFilter;

    public DbManager(Storage storage, String pathPrefix, DbDownloader dbDownloader,
                     DbUpdateRequester dbUpdateRequester, CommunityDatabase communityDatabase) {
        this.storage = storage;
        this.pathPrefix = pathPrefix;
        this.dbDownloader = dbDownloader;
        this.dbUpdateRequester = dbUpdateRequester;
        this.communityDatabase = communityDatabase;
    }

    public void setNumberFilter(NumberFilter numberFilter) {
        this.numberFilter = numberFilter;
    }

    public boolean downloadMainDb() {
        return downloadMainDb(DEFAULT_URL);
    }

    public boolean downloadMainDb(String url) {
        LOG.debug("downloadMainDb() started");

        File dataDir = new File(storage.getDataDirPath());

        String siaDir = pathPrefix;
        String tmpUpdateDir = siaDir.substring(0, siaDir.indexOf('/')) + "-tmp/";
        String oldDir = siaDir.substring(0, siaDir.indexOf('/')) + "-old/";

        com.chromatics.caller_id.utils.FileUtils.delete(dataDir, tmpUpdateDir);
        FileUtils.createDirectory(dataDir, tmpUpdateDir);
        LOG.debug("downloadMainDb() prepared dirs");

        if (dbDownloader.download(url, storage.getDataDirPath() + tmpUpdateDir, getDownloadFileProcessor())) {
            LOG.debug("downloadMainDb() downloaded and unpacked");

            File old = new File(dataDir, siaDir);
            if (old.exists() && !old.renameTo(new File(dataDir, oldDir))) {
                LOG.warn("downloadMainDb() couldn't rename sia to old");
                return false;
            }

            if (!new File(dataDir, tmpUpdateDir).renameTo(new File(dataDir, siaDir))) {
                LOG.warn("downloadMainDb() couldn't rename tmp to sia");
                return false;
            }

            FileUtils.delete(dataDir, oldDir);

            LOG.debug("downloadMainDb() folders moved");
            return true;
        } else {
            LOG.warn("downloadMainDb() failed downloading");
        }

        return false;
    }

    private DbDownloader.FileProcessor getDownloadFileProcessor() {
        if (numberFilter == null) return null;

        com.chromatics.caller_id.common.interfaces.NumberFilter numberFilter = adaptFilterToFileNames(this.numberFilter);

        final String dataNamePrefix = communityDatabase.getNamePrefix();
        final int dataNamePrefixLength = dataNamePrefix.length();
        final String dataNamePostfix = ".dat";

        return new DbDownloader.FileProcessor() {
            @Override
            public boolean shouldUnpack(String name) {
                return !isMainSlice(name) || numberFilter.isDetailed() || numberFilter.keepPrefix(name);
            }

            @Override
            public void process(String name, File file) {
                if (isMainSlice(name) && numberFilter.isDetailed()) {
                    filterOrDeleteSliceFile(file);
                }
            }

            boolean isMainSlice(String name) {
                return name.startsWith(dataNamePrefix) && name.endsWith(dataNamePostfix)
                        && name.charAt(dataNamePrefixLength) >= '0' && name.charAt(dataNamePrefixLength) <= '9';
            }
        };
    }

    public void removeMainDb() {
        LOG.debug("removeMainDb() started");

        FileUtils.delete(storage.getDataDirPath(), pathPrefix);

        LOG.debug("removeMainDb() finished");
    }

    public UpdateResult updateSecondaryDb() {
        return updateSecondaryDb(false);
    }

    public UpdateResult updateSecondaryDb(boolean keepFile) {
        LOG.info("updateSecondaryDb({}) started", keepFile);

        if (!communityDatabase.isOperational()) {
            LOG.warn("updateSecondaryDb() DB is not operational, update aborted");
            return new UpdateResult(false, UpdateResult.Status.UNKNOWN_ERROR);
        }

        long startTimestamp = System.currentTimeMillis();

        boolean updated = false;
        UpdateResult.Status result = null;

        int lastVer = communityDatabase.getEffectiveDbVersion();

        for (int i = 0; i < 1000; i++) {
            result = updateSecondaryDbInternal(keepFile);
            LOG.debug("updateSecondaryDb() internal update result: {}", result);

            if (result != UpdateResult.Status.UPDATED) break;

            int newVer = communityDatabase.getEffectiveDbVersion();
            LOG.debug("updateSecondaryDb() DB version after update: {}", newVer);

            if (newVer <= lastVer) {
                LOG.error("updateSecondaryDb() effective DB version did not increase: {}->{}", lastVer, newVer);
                result = UpdateResult.Status.UNKNOWN_ERROR;
                break;
            }

            lastVer = newVer;
            updated = true;
        }

        if (updated) {
            LOG.info("updateSecondaryDb() new DB version: {}", communityDatabase.getEffectiveDbVersion());
        }

        LOG.info("updateSecondaryDb() finished in {} ms", System.currentTimeMillis() - startTimestamp);

        return new UpdateResult(updated, result);
    }

    private UpdateResult.Status updateSecondaryDbInternal(boolean keepFile) {
        LOG.debug("updateSecondaryDbInternal() started");

        long startTimestamp = System.currentTimeMillis();

        int effectiveDbVersion = communityDatabase.getEffectiveDbVersion();
        LOG.debug("updateSecondaryDbInternal() effectiveDbVersion={}", effectiveDbVersion);

        File tempFile = null;
        int sliceVersion = 0;

        boolean updateDownloaded = false;

        try {
            DbUpdateRequester.Update update = dbUpdateRequester.getUpdate(effectiveDbVersion);

            if (!update.hasUpdate()) {
                LOG.debug("updateSecondaryDbInternal() no update; returning result: {}", update.getResult());

                switch (update.getResult()) {
                    case NO_UPDATES: return UpdateResult.Status.NO_UPDATE;
                    case OUTDATED_APP: return UpdateResult.Status.OUTDATED_APP;
                    case BAD_SECONDARY: return UpdateResult.Status.BAD_SECONDARY;
                    case UNEXPECTED_RESPONSE: return UpdateResult.Status.UNEXPECTED_RESPONSE;
                    default: throw new RuntimeException("Unexpected result");
                }
            }

            int totalRead = 0;

            try (InputStream bodyStream = update.getBody().byteStream();
                 InputStream is = new GZIPInputStream(bodyStream)) {
                LOG.trace("updateSecondaryDbInternal() saving response data to file");

                tempFile = File.createTempFile("sia", "database", new File(storage.getCacheDirPath()));

                try (OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buff = new byte[10240];

                    while (true) {
                        int read = is.read(buff);
                        if (read == -1) {
                            break;
                        }
                        out.write(buff, 0, read);
                        totalRead += read;
                    }
                }

                LOG.trace("updateSecondaryDbInternal() finished saving response data to file; totalRead={}", totalRead);
            }

            if (totalRead == 0) {
                LOG.warn("updateSecondaryDbInternal() no data");
                return UpdateResult.Status.UNEXPECTED_RESPONSE;
            }

            updateDownloaded = true;

            try (FileInputStream fis = new FileInputStream(tempFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                LOG.trace("updateSecondaryDbInternal() updating DB");
                sliceVersion = communityDatabase.updateSecondary(bis, numberFilter);
                LOG.trace("updateSecondaryDbInternal() finished updating DB");
            }

            LOG.debug("updateSecondaryDbInternal() updated performed successfully in {} ms",
                    System.currentTimeMillis() - startTimestamp);

            return UpdateResult.Status.UPDATED;
        } catch (Exception e) {
            LOG.error("updateSecondaryDbInternal()", e);
        } finally {
            if (tempFile != null) {
                try {
                    if (keepFile) {
                        String name = "sia-update_" + effectiveDbVersion + "-" + sliceVersion
                                + "_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date());
                        File newFile = new File(storage.getCacheDirPath(), name);

                        if (!tempFile.renameTo(newFile)) {
                            LOG.warn("updateSecondaryDbInternal() failed to rename tempFile {} -> {}",
                                    tempFile, newFile);
                        }
                    } else if (!tempFile.delete()) {
                        LOG.warn("updateSecondaryDbInternal() failed to delete tempFile {}", tempFile);
                    }
                } catch (Exception e) {
                    LOG.warn("updateSecondaryDbInternal() exception in finally", e);
                }
            }

            LOG.debug("updateSecondaryDbInternal() elapsed time: {} ms", System.currentTimeMillis() - startTimestamp);
        }
        return updateDownloaded ? UpdateResult.Status.UNKNOWN_ERROR : UpdateResult.Status.DOWNLOAD_ERROR;
    }

    public void filterDb() {
        LOG.debug("filter() started");

        if (numberFilter == null) {
            LOG.info("filter() numberFilter is null, aborting");
            return;
        }

        File mainDir = new File(storage.getDataDirPath(), communityDatabase.getPathPrefix());
        if (mainDir.isDirectory() && communityDatabase.isOperational() && !communityDatabase.isUsingInternal()) {
            LOG.debug("filter() processing main DB");

            com.chromatics.caller_id.common.interfaces.NumberFilter numberFilterForFiles = adaptFilterToFileNames(numberFilter);

            final String dataNamePrefix = communityDatabase.getNamePrefix();
            final int dataNamePrefixLength = dataNamePrefix.length();
            final String dataNamePostfix = ".dat";

            FilenameFilter dataFileFilter = (d, name) ->
                    name.startsWith(dataNamePrefix) && name.endsWith(dataNamePostfix)
                            && name.charAt(dataNamePrefixLength) >= '0' && name.charAt(dataNamePrefixLength) <= '9';

            // avoid loading 30k+ `File`s at once
            for (int i = 0; i < 10; i++) {
                char j = (char) ('0' + i);
                FilenameFilter filter = (d, name) -> dataFileFilter.accept(d, name)
                        && name.charAt(dataNamePrefixLength) == j;

                File[] files = mainDir.listFiles(filter);
                if (files != null) {
                    for (File file : files) {
                        filterOrDeleteSliceFile(file, numberFilterForFiles, file.getName());
                    }
                }
            }
        } else {
            LOG.debug("filter() no main dir, not operational or embedded DB - not filtering");
        }

        File secondaryDir = new File(storage.getDataDirPath(), communityDatabase.getSecondaryDbPathPrefix());
        if (secondaryDir.isDirectory()) {
            LOG.debug("filter() processing secondary DB");

            for (int sliceId = 0; sliceId <= 99; sliceId++) {
                String slicePath = communityDatabase.getCachedSecondarySliceFilePath(sliceId);
                if (slicePath != null) {
                    filterOrDeleteSliceFile(new File(storage.getDataDirPath(), slicePath),
                            numberFilter, String.valueOf(sliceId));
                }
            }
        } else {
            LOG.debug("filter() no secondary dir");
        }

        LOG.debug("filter() finished");
    }

    private void filterOrDeleteSliceFile(File file) {
        filterOrDeleteSliceFile(file, numberFilter, null);
    }

    private void filterOrDeleteSliceFile(File file, com.chromatics.caller_id.common.interfaces.NumberFilter numberFilter, String slicePrefix) {
        LOG.trace("filterOrDeleteSliceFile() started, file={}, slicePrefix={}", file, slicePrefix);

        boolean keepFile = false;

        if (numberFilter.isDetailed()) {
            keepFile = filterSliceFile(file);
        } else if (slicePrefix != null && numberFilter.keepPrefix(slicePrefix)) {
            keepFile = true;
        }

        if (!keepFile && !file.delete()) {
            LOG.warn("filterOrDeleteSliceFile() failed to delete DB file: {}", file);
        }

        LOG.trace("filterOrDeleteSliceFile() finished, file={}", file);
    }

    private boolean filterSliceFile(File file) {
        LOG.trace("filterSliceFile() filtering slice");

        boolean notEmpty = false;

        CommunityDatabaseDataSlice originalSlice = communityDatabase.createDbDataSlice();
        try {
            try (InputStream is = new FileInputStream(file);
                 BufferedInputStream stream = new BufferedInputStream(is)) {
                originalSlice.loadFromStream(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            CommunityDatabaseDataSlice slice = communityDatabase.createDbDataSlice();
            if (slice.partialClone(originalSlice, originalSlice.generateFilteredIndex(numberFilter))) {
                LOG.trace("filterSliceFile() writing filtered slice");
                try (FileOutputStream fos = new FileOutputStream(file);
                     BufferedOutputStream stream = new BufferedOutputStream(fos)) {
                    slice.writeMerged(null, stream);
                }
                LOG.trace("filterSliceFile() finished writing filtered slice");

                notEmpty = true;
            }
        } catch (IOException e) {
            LOG.warn("filterSliceFile() error filtering slice", e);
        }

        LOG.trace("filterSliceFile() finished processing slice");
        return notEmpty;
    }

    private com.chromatics.caller_id.common.interfaces.NumberFilter adaptFilterToFileNames(NumberFilter numberFilter) {
        if (numberFilter == null) return null;

        final String dataNamePrefix = communityDatabase.getNamePrefix();
        final int dataNamePrefixLength = dataNamePrefix.length();
        final String dataNamePostfix = ".dat";

        return new com.chromatics.caller_id.common.interfaces.NumberFilter() {
            @Override
            public boolean keepPrefix(String name) {
                String numberPart = name.substring(dataNamePrefixLength, name.lastIndexOf(dataNamePostfix));
                return numberFilter.keepPrefix(numberPart);
            }

            @Override
            public boolean keepNumber(String number) {
                return numberFilter.keepNumber(number);
            }

            @Override
            public boolean isDetailed() {
                return numberFilter.isDetailed();
            }
        };
    }

}
