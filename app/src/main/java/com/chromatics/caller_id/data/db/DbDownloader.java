package com.chromatics.caller_id.data.db;

import static java.util.Objects.requireNonNull;

import com.chromatics.caller_id.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Okio;
import okio.Sink;

public class DbDownloader {

    public interface FileProcessor {
        boolean shouldUnpack(String name);

        void process(String name, File file);
    }

    private static final Logger LOG = LoggerFactory.getLogger(DbDownloader.class);

    private final OkHttpClient okHttpClientFactory;

    public DbDownloader(OkHttpClient okHttpClientFactory) {
        this.okHttpClientFactory = okHttpClientFactory;
    }

    public boolean download(String url, String path, FileProcessor fileProcessor) {
        LOG.info("download() started; path: {}", path);

        LOG.debug("download() making a request");
        Request request = new Request.Builder().url(url).build();
        DownloadResult result = download(path, request, fileProcessor);

        if (result.success) return true;

        if (result.responseCode == 403) { // probably captcha
            LOG.debug("download() got 403, trying a workaround");

            // make the server happy with fake headers
            request = new Request.Builder()
                    .url(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:68.0) Gecko/20100101 Firefox/68.0")
                    .header("Accept-Encoding", "gzip, deflate, br") // may break things
                    .build();

            result = download(path, request, fileProcessor);
        }

        return result.success;
    }

    private static class DownloadResult {
        boolean success;
        int responseCode;

        DownloadResult(boolean success, int responseCode) {
            this.success = success;
            this.responseCode = responseCode;
        }
    }

    private DownloadResult download(String path, Request request, FileProcessor fileProcessor) {
        int responseCode = -1;

        try (Response response = okHttpClientFactory.newCall(request).execute()) {
            if (response.isSuccessful()) {
                LOG.debug("download() got successful response");

                LOG.trace("download() processing zip");
                processZipStream(requireNonNull(response.body()).byteStream(), path, fileProcessor);
                LOG.trace("download() zip processed");

                LOG.debug("download() finished successfully");
                return new DownloadResult(true, response.code());
            } else {
                LOG.warn("download() unsuccessful response {}", response);
                responseCode = response.code();
            }
        } catch (IOException e) {
            LOG.warn("download()", e);
        }

        LOG.debug("download() finished unsuccessfully");
        return new DownloadResult(false, responseCode);
    }

    private void processZipStream(InputStream inputStream, String path, FileProcessor fileProcessor)
            throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            for (ZipEntry zipEntry; (zipEntry = zipInputStream.getNextEntry()) != null; ) {
                String name = zipEntry.getName();

                if (zipEntry.isDirectory()) {
                    FileUtils.createDirectory(path + name);
                    continue;
                }

                if (fileProcessor != null && !fileProcessor.shouldUnpack(name)) continue;

                File file = new File(path + name);
                try (Sink out = Okio.sink(file)) {
                    Okio.buffer(Okio.source(zipInputStream)).readAll(out);
                }

                if (fileProcessor != null) {
                    fileProcessor.process(name, file);
                }
            }
        }
    }

}
