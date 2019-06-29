package dev.iusupov.vkphotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Loads a bitmap from the given URL.
 * If the loading fails it'll retry it two times after 3 and 6 seconds respectively.
 *
 * @return Bitmap or null
 *
 * @throws IOException if the loading fails.
 * @throws SocketTimeoutException if establishing connection takes more than 3 seconds.
 */
@Throws(IOException::class, SocketTimeoutException::class)
fun CoroutineScope.loadBitmapAsync(url: URL): Deferred<Bitmap?> {
    Timber.i("Start loading bitmap $url")

    return async(Dispatchers.Default) {
        var connection: HttpsURLConnection? = null
        var result: Bitmap? = null

        repeat(3) { attempt ->
            val delayInMillis = 3_000L * attempt
            delay(delayInMillis)

            try {
                connection = establishGetConnection(url)
                result = connection?.inputStream?.use {
                    BitmapFactory.decodeStream(it)
                }
                return@async result
            } catch (error: Exception) {
                Timber.e("Error while loading a bitmap: attempt=$attempt, url=$url. $error")
                if (attempt == 2) {
                    throw error
                }
            } finally {
                connection?.disconnect()
            }
        }

        result
    }
}

@Throws(IOException::class)
private suspend fun establishGetConnection(url: URL): HttpsURLConnection? {
    return withContext(Dispatchers.IO) {
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