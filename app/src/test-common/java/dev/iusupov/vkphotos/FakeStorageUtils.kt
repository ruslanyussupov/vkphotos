package dev.iusupov.vkphotos

import dev.iusupov.vkphotos.utils.StorageUtils

class FakeStorageUtils : StorageUtils {

    override suspend fun writeToCache(url: String, byteArray: ByteArray) {

    }

    override suspend fun readFromCache(url: String): ByteArray? {
        return null
    }
}