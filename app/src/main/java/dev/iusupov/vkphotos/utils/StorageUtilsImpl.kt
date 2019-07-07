package dev.iusupov.vkphotos.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageUtilsImpl(private val context: Context) : StorageUtils {

    private fun getTempFile(url: String): File? {
        return Uri.parse(url)?.lastPathSegment?.let { fileName ->
            val file = File(getTempDir(), "$fileName.tmp")
            file.createNewFile()
            file
        }
    }

    private fun getTempDir(): File {
        val tempDir = File(context.filesDir, "/temp/")
        if (!tempDir.exists()) {
            tempDir.mkdir()
        }
        return tempDir
    }

    override suspend fun writeToCache(url: String, byteArray: ByteArray) {
        withContext(Dispatchers.IO) {
            getTempFile(url)?.writeBytes(byteArray)
        }
    }

    override suspend fun readFromCache(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            getTempFile(url)?.readBytes()
        }
    }
}