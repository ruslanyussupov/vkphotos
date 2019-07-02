package dev.iusupov.vkphotos.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class StorageUtilsImpl(private val context: Context) : StorageUtils {

    private fun getTempFile(url: String): File? {
        return Uri.parse(url)?.lastPathSegment?.let { fileName ->
            Timber.d("#getTempFile() $fileName")
            val file = File(context.filesDir, "$fileName.tmp")
            file.createNewFile()
            file
        }
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