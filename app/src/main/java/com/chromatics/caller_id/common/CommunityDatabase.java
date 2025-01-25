package com.chromatics.caller_id.common;

import com.chromatics.caller_id.Storage;
import com.chromatics.caller_id.models.CommunityDatabaseDataSlice;
import com.chromatics.caller_id.models.CommunityDatabaseItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommunityDatabase extends AbstractDatabase<CommunityDatabaseDataSlice, CommunityDatabaseItem> {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityDatabase.class);

    private final String secondaryPathPrefix;
    private final Settings settings;

    private final Map<Integer, CommunityDatabaseDataSlice> secondarySliceCache = new HashMap<>();

    private final Map<Integer, Boolean> existingSecondarySliceFiles = new HashMap<>();

    public CommunityDatabase(Storage storage, Source source,
                             String pathPrefix, String secondaryPathPrefix,
                             Settings settings) {
        super(storage, source, pathPrefix);
        this.secondaryPathPrefix = secondaryPathPrefix;
        this.settings = settings;
    }

    public int getEffectiveDbVersion() {
        checkLoaded();

        int secondaryDbVersion = settings.getSecondaryDbVersion();
        return secondaryDbVersion > 0 ? secondaryDbVersion : baseDatabaseVersion;
    }

    @Override
    public String getNamePrefix() {
        return "data_slice_";
    }

    @Override
    protected void reset() {
        super.reset();

        resetCaches();
    }

    private void resetCaches() {
        secondarySliceCache.clear();
        existingSecondarySliceFiles.clear();
    }

    @Override
    protected void loadInfoDataAfterLoadedHook(boolean useInternal) {
        int oldDbVersion = settings.getBaseDbVersion();
        if (baseDatabaseVersion != oldDbVersion) {
            LOG.info("loadInfoDataAfterLoadedHook() base version changed; resetting secondary DB;" +
                    " oldDbVersion={}, baseDatabaseVersion={}", oldDbVersion, baseDatabaseVersion);
            resetSecondaryDatabase();
            settings.setBaseDbVersion(baseDatabaseVersion);
        }
    }

    @Override
    protected CommunityDatabaseItem getDbItemByNumberInternal(long number) {
        LOG.debug("getDbItemByNumberInternal({}) started", number);

        CommunityDatabaseDataSlice secondarySlice = getSecondaryDataSlice(number);

        CommunityDatabaseItem communityDatabaseItem = secondarySlice != null
                ? secondarySlice.getDbItemByNumber(number) : null;

        if (communityDatabaseItem == null) {
            LOG.trace("getDbItemByNumberInternal() not found in secondary DB");
            CommunityDatabaseDataSlice baseSlice = getDataSlice(number);
            communityDatabaseItem = baseSlice != null ? baseSlice.getDbItemByNumber(number) : null;
        }

        LOG.trace("getDbItemByNumberInternal() communityDatabaseItem={}", communityDatabaseItem);

        if (communityDatabaseItem != null && !communityDatabaseItem.hasRatings()) {
            communityDatabaseItem = null;
        }

        return communityDatabaseItem;
    }

    @Override
    public CommunityDatabaseDataSlice createDbDataSlice() {
        return new CommunityDatabaseDataSlice();
    }

    private CommunityDatabaseDataSlice getSecondaryDataSlice(long number) {
        LOG.debug("getSecondaryDataSlice({}) started", number);

        if (number <= 0) return null;

        String numberString = String.valueOf(number);
        if (numberString.length() < 2) return null;

        int sliceId = Integer.parseInt(numberString.substring(0, 2));
        LOG.trace("getSecondaryDataSlice() sliceId={}", sliceId);

        CommunityDatabaseDataSlice communityDatabaseDataSlice = secondarySliceCache.get(sliceId);
        if (communityDatabaseDataSlice == null) {
            LOG.trace("getSecondaryDataSlice() trying to load slice with sliceId={}", sliceId);

            communityDatabaseDataSlice = createDbDataSlice();
            String path = getCachedSecondarySliceFilePath(sliceId);
            if (path != null) {
                LOG.trace("getSecondaryDataSlice() slice file exists, loading from: {}", path);
                loadSlice(communityDatabaseDataSlice, path, false);
            } else {
                LOG.trace("getSecondaryDataSlice() slice file doesn't exist");
            }
            secondarySliceCache.put(sliceId, communityDatabaseDataSlice);
        } else {
            LOG.trace("getSecondaryDataSlice() found slice in cache");
        }
        return communityDatabaseDataSlice;
    }

    public String getCachedSecondarySliceFilePath(int id) {
        String path = getSecondarySliceFilePath(id);
        Boolean exists = existingSecondarySliceFiles.get(id);
        if (exists == null) {
            exists = new File(storage.getDataDirPath() + path).exists();
            existingSecondarySliceFiles.put(id, exists);
        }
        return exists ? path : null;
    }

    private String getSecondarySliceFilePath(int id) {
        return getSecondaryDbPathPrefix() + id + ".sia";
    }

    public void resetSecondaryDatabase() {
        LOG.debug("resetSecondaryDatabase() started");

        File dir = new File(storage.getDataDirPath(), getSecondaryDbPathPrefix());
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    LOG.warn("resetSecondaryDatabase() failed to delete secondary DB file: {}", file.getAbsolutePath());
                }
            }
        }

        secondarySliceCache.clear();
        existingSecondarySliceFiles.clear();
        settings.setSecondaryDbVersion(0);

        LOG.info("resetSecondaryDatabase() secondary DB was reset");
    }

    public String getSecondaryDbPathPrefix() {
        return secondaryPathPrefix;
    }

//    private void createSecondaryDbDirectory() {
//        FileUtils.createDirectory(storage.getDataDirPath(), getSecondaryDbPathPrefix());
//    }

    public int updateSecondary(InputStream inputStream, NumberFilter numberFilter) throws IOException {
        LOG.trace("updateSecondary() loading slice from input stream");
        CommunityDatabaseDataSlice slice = createDbDataSlice();
        slice.loadFromStream(inputStream);

        LOG.trace("updateSecondary() distributing slice");
        updateSecondaryWithSlice(slice, numberFilter);

        return slice.getDbVersion();
    }

    protected void updateSecondaryWithSlice(CommunityDatabaseDataSlice dataSlice, NumberFilter numberFilter)
            throws IOException {
        LOG.debug("updateSecondaryWithSlice() started");
        LOG.trace("updateSecondaryWithSlice() dataSlice={}", dataSlice);

//        createSecondaryDbDirectory();

        secondarySliceCache.clear(); // free up some RAM

        long startTimestamp = System.currentTimeMillis();

        CommunityDatabaseDataSlice.SliceIndexMap sliceIndexMap = dataSlice.generateShortSliceIndexMap(numberFilter);

        ArrayList<Integer> updatedIndexes = new ArrayList<>();
        for (int sliceId = 0; sliceId <= 99; sliceId++) {
            CommunityDatabaseDataSlice newSlice = createDbDataSlice();
            if (newSlice.partialClone(dataSlice, sliceIndexMap.takeIndex(sliceId))) {
                String filePath = getSecondarySliceFilePath(sliceId);

                CommunityDatabaseDataSlice sliceFromExistingFile = createDbDataSlice();
                if (getCachedSecondarySliceFilePath(sliceId) != null) {
                    loadSliceUnsafe(sliceFromExistingFile, filePath, false);
                }

                try (BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(storage.getDataDirPath() + filePath + ".update", false))) {
                    sliceFromExistingFile.writeMerged(newSlice, stream);
                }

                updatedIndexes.add(sliceId);

                LOG.debug("updateSecondaryWithSlice() added {} items to sliceId={}",
                        newSlice.getNumberOfItems(), sliceId);
            }
        }

        LOG.debug("updateSecondaryWithSlice() update files created, renaming files");

        for (int sliceId : updatedIndexes) {
            String filePath = storage.getDataDirPath() + getSecondarySliceFilePath(sliceId);

            File updatedFile = new File(filePath + ".update");
            File oldFile = new File(filePath);
            if (oldFile.exists() && !oldFile.delete()) {
                throw new IOException("Can't delete " + filePath);
            }
            if (!updatedFile.renameTo(oldFile)) {
                throw new IOException("Can't replace slice " + updatedFile);
            }
        }

        settings.setSecondaryDbVersion(dataSlice.getDbVersion());
        resetCaches();

        LOG.debug("updateSecondaryWithSlice() finished in {} ms", System.currentTimeMillis() - startTimestamp);
    }

}
