package com.chromatics.caller_id.common

import android.content.Context
import com.chromatics.caller_id.Storage
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class AndroidStorage(private val context: Context) : Storage {
    override fun getDataDirPath(): String {
        return context.filesDir.absolutePath + "/"
    }

    override fun getCacheDirPath(): String {
        return context.cacheDir.absolutePath + "/"
    }

    @Throws(IOException::class)
    override fun openFile(fileName: String, internal: Boolean): InputStream {
        return if (internal) {
            context.assets.open(fileName)
        } else {
            FileInputStream(dataDirPath + fileName)
        }
    }
}
