package dev.iusupov.vkphotos.utils


interface StorageUtils {

    suspend fun writeToCache(url: String, byteArray: ByteArray)

    suspend fun readFromCache(url: String): ByteArray?
}