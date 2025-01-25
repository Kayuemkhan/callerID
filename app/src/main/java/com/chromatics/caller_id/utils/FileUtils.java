package com.chromatics.caller_id.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.chromatics.caller_id.BuildConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class FileUtils {

    private static final String FILEPROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static void shareFile(Activity activity, File file) {
        try {
            Uri uri = FileProvider.getUriForFile(activity, FILEPROVIDER_AUTHORITY, file);

            ShareCompat.IntentBuilder shareBuilder = ShareCompat.IntentBuilder.from(activity)
                    .setStream(uri)
                    .setType(activity.getContentResolver().getType(uri));

            shareBuilder.getIntent().addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            shareBuilder.startChooser();
        } catch (Exception e) {
            LOG.warn("shareFile()", e);
        }
    }

    public static File getExternalFilesDir(Context context) {
        File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
        for (File dir : dirs) {
            if (dir != null) return dir;
        }
        return null;
    }

    public static File createDirectory(String path) {
        return createDirectory(new File(path));
    }

    public static File createDirectory(String base, String dir) {
        return createDirectory(new File(base, dir));
    }

    public static File createDirectory(File base, String dir) {
        return createDirectory(new File(base, dir));
    }

    public static File createDirectory(File d) {
        if (!d.exists()) {
            LOG.debug("createDirectory() creating: {}, result: {}", d.getAbsolutePath(), d.mkdir());
        }
        if (!d.isDirectory()) {
            LOG.error("createDirectory() is not a directory: {}", d.getAbsolutePath());
        }
        return d;
    }

    public static void delete(String base, String name) {
        delete(new File(base, name));
    }

    public static void delete(File base, String name) {
        delete(new File(base, name));
    }

    public static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] list = file.listFiles();
                if (list != null) {
                    for (File f : list) {
                        delete(f);
                    }
                }
            }
            file.delete();
        }
    }
}
