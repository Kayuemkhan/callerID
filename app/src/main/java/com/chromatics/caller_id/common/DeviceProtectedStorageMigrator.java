package com.chromatics.caller_id.common;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class DeviceProtectedStorageMigrator {

    public void migrate(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        try {
            Context dpsContext = context.createDeviceProtectedStorageContext();

            if (new com.chromatics.caller_id.utils.Settings(dpsContext).getInt(com.chromatics.caller_id.utils.Settings.SYS_PREFERENCES_VERSION, -1) != -1) return;
            if (new com.chromatics.caller_id.utils.Settings(context).getInt(com.chromatics.caller_id.utils.Settings.SYS_PREFERENCES_VERSION, -1) == -1) return;

            copyDir(context, dpsContext, SiaConstants.SIA_PATH_PREFIX);
            copyDir(context, dpsContext, SiaConstants.SIA_SECONDARY_PATH_PREFIX);

            dpsContext.moveSharedPreferencesFrom(context, SiaConstants.CALLER_ID_PROPERTIES);
            dpsContext.moveSharedPreferencesFrom(context, context.getPackageName() + "_preferences");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyDir(Context srcContext, Context dstContext, String dir) throws IOException {
        File srcDir = new File(srcContext.getFilesDir(), dir);
        if (srcDir.exists()) {
            File dstDir = new File(dstContext.getFilesDir(), dir);

            dstDir.mkdir();
            copyDirectoryContent(srcDir, dstDir);

//            FileUtils.delete(srcDir);
        }
    }

    private static void copyDirectoryContent(File src, File dst) throws IOException {
        for (String file : src.list()) {
            File srcFile = new File(src, file);
            File dstFile = new File(dst, file);

            dstFile.createNewFile();
            try (FileChannel source = new FileInputStream(srcFile).getChannel();
                 FileChannel destination = new FileOutputStream(dstFile).getChannel()) {
                destination.transferFrom(source, 0, source.size());
            }
        }
    }

}
