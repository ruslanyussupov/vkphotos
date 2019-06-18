package dev.iusupov.vkphotos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection


@Throws(IOException::class, SocketTimeoutException::class)
fun CoroutineScope.loadBitmap(url: URL): Deferred<Bitmap?> {
    Timber.i("Start loading bitmap $url")

    return async(Dispatchers.Default) {
        getInputStream(url).use {
            BitmapFactory.decodeStream(it)
        }
    }
}

suspend fun getInputStream(url: URL): InputStream? {
    var connection: HttpsURLConnection? = null
    try {
        return withContext(Dispatchers.IO) {
            connection = url.openConnection() as? HttpsURLConnection
            connection?.run {
                readTimeout = 3_000
                connectTimeout = 3_000
                requestMethod = "GET"
                connect()

                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }

                inputStream
            }
        }

    } finally {
        connection?.disconnect()
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