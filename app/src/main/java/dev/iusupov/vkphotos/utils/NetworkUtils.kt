package dev.iusupov.vkphotos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NetworkUtils(private val storageUtils: StorageUtils) {

    private var networkDispatcher = Dispatchers.IO
    private var computationDispatcher = Dispatchers.Default

    // TODO: Make a service responsible for clean up the cache
    suspend fun loadBitmapWithCaching(url: String): Bitmap? {
        Timber.i("Start loading bitmap with caching from $url")

        val cache = storageUtils.readFromCache(url)

        return if (cache == null || cache.isEmpty()) {
            val byteArray = parseUrlFromString(url)?.let { _url ->
                fetchByteArray(_url)
            }
            byteArray?.let { _byteArray ->
                storageUtils.writeToCache(url, _byteArray)
                withContext(computationDispatcher) {
                    BitmapFactory.decodeByteArray(_byteArray, 0, _byteArray.size)
                }
            }
        } else {
            withContext(computationDispatcher) {
                BitmapFactory.decodeByteArray(cache, 0, cache.size)
            }
        }
    }

    suspend fun loadBitmap(url: String): Bitmap? {
        Timber.i("Start loading bitmap from $url")

        val byteArray = parseUrlFromString(url)?.let { _url ->
            fetchByteArray(_url)
        }
        return byteArray?.let { _byteArray ->
            withContext(computationDispatcher) {
                BitmapFactory.decodeByteArray(_byteArray, 0, _byteArray.size)
            }
        }
    }

    /**
     * Gets a byte array from the given URL.
     * If the loading fails it'll retry it two times after 2 and 4 seconds respectively.
     *
     * @return ByteArray?
     *
     */
    suspend fun fetchByteArray(url: URL): ByteArray? {
        Timber.i("Start fetching bytes from $url")

        var connection: HttpsURLConnection? = null
        var result: ByteArray? = null

        repeat(3) { attempt ->
            val delayInMillis = 2_000L * attempt
            delay(delayInMillis)

            try {
                connection = establishGetConnection(url)
                Timber.i("Connection for $url is established")
                result = withContext(networkDispatcher) {
                    connection?.inputStream?.use {
                        it.readBytes()
                    }
                }
                return result
            } catch (error: Exception) {
                Timber.e("Error while getting a byte array: attempt=$attempt, url=$url. $error")
                if (attempt == 2) {
                    return null
                }
            } finally {
                connection?.disconnect()
            }
        }

        return result
    }

    @Throws(IOException::class, SocketTimeoutException::class)
    private suspend fun establishGetConnection(url: URL): HttpsURLConnection? {
        return withContext(networkDispatcher) {
            val connection = url.openConnection() as? HttpsURLConnection
            connection?.apply {
                readTimeout = 3_000
                connectTimeout = 3_000
                requestMethod = "GET"
                connect()

                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }
            }

            connection
        }
    }

    @VisibleForTesting
    fun swapNetworkDispatcher(dispatcher: CoroutineDispatcher) {
        networkDispatcher = dispatcher
    }

    @VisibleForTesting
    fun swapComputationDispatcher(dispatcher: CoroutineDispatcher) {
        computationDispatcher = dispatcher
    }
}

fun parseUrlFromString(url: String): URL? {
    var result: URL? = null
    try {
        result = URL(url)
    } catch (e: MalformedURLException) {
        Timber.e("Bad URL: $url. $e")
    }
    return result
}

fun hasNetworkConnection(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnected == true
}