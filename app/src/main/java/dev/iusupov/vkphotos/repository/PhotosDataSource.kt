package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.vk.api.sdk.exceptions.VKApiExecutionException
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.LinkedList

// TODO: Maybe it's better to move requesting into independent class
class PhotosDataSource(private val ownerId: Int,
                       private val api: Api,
                       private val networkUtils: NetworkUtils,
                       private val coroutineScope: CoroutineScope) : PositionalDataSource<PhotoItem>() {

    val failed = LinkedList<suspend () -> Unit>()
    private val _loadMoreNetworkState = MutableLiveData<NetworkState>()
    private val _loadInitialNetworkState = MutableLiveData<NetworkState>()
    val loadMoreNetworkState: LiveData<NetworkState> = _loadMoreNetworkState
    val loadInitialNetworkState: LiveData<NetworkState> = _loadInitialNetworkState

    val retryFailed = {
        while (failed.isNotEmpty()) {
            val last = failed.removeLast()
            coroutineScope.launch {
                last.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<PhotoItem>) {
        val count = params.requestedLoadSize
        val offset = params.requestedStartPosition
        coroutineScope.launch {
            requestInitial(count, offset, callback)
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<PhotoItem>) {
        val count = params.loadSize
        val offset = params.startPosition
        coroutineScope.launch {
            requestRange(count, offset, callback)
        }
    }

    suspend fun requestInitial(count: Int, offset: Int, callback: LoadInitialCallback<PhotoItem>) {
        if (!coroutineScope.isActive) return

        Timber.d("Request initial: count=$count, offset=$offset.")

        _loadInitialNetworkState.postValue(Loading)

        repeat(3) { attempt ->
            val delayInMillis = 2_000L * attempt
            delay(delayInMillis)

            try {
                val result = api.fetchPhotos(ownerId, count, offset)
                Timber.i("Requesting photos completed: count=$count, offset=$offset, result=${result.photos.size}: ${result.photos}")
                val photoItems = convertToPhotoItems(result.photos)
                Timber.i("Converted to photo items: size=${photoItems.size}")
                callback.onResult(photoItems, offset, result.count)
                if (photoItems.isEmpty()) {
                    _loadInitialNetworkState.postValue(
                        Error(
                            message = "No data.",
                            code = Error.ERROR_CODE_NO_DATA
                        )
                    )
                } else {
                    _loadInitialNetworkState.postValue(Loaded)
                }
                return

            } catch (error: VKApiExecutionException) {
                Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                val errorMsg = error.errorMsg ?: "Requesting photos is failed."

                if (error.code == Error.ERROR_CODE_PRIVATE_PROFILE) {
                    _loadInitialNetworkState.postValue(Error(errorMsg, error.code))
                    return
                } else if (attempt == 2) {
                    failed.addFirst { requestInitial(count, offset, callback) }
                    _loadInitialNetworkState.postValue(Error(errorMsg, error.code))
                }

            } catch (error: Exception) {
                Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                if (attempt == 2) {
                    failed.addFirst { requestInitial(count, offset, callback) }
                    val errorMsg = error.message ?: "Requesting photos is failed."
                    _loadInitialNetworkState.postValue(Error(errorMsg))
                }
            }
        }
    }

    suspend fun requestRange(count: Int, offset: Int, callback: LoadRangeCallback<PhotoItem>) {
        if (!coroutineScope.isActive) return
        Timber.i("Requesting range: count=$count, offset=$offset.")

        _loadMoreNetworkState.postValue(Loading)

        repeat(3) { attempt ->
            val delayInMillis = 2_000L * attempt
            delay(delayInMillis)

            try {
                val result = api.fetchPhotos(ownerId, count, offset)
                Timber.i("Requesting photos completed: count=$count, offset=$offset, result=${result.photos.size}: ${result.photos}")
                val photoItems = convertToPhotoItems(result.photos)
                Timber.i("Converted to photo items: size=${photoItems.size}")
                callback.onResult(photoItems)
                _loadMoreNetworkState.postValue(Loaded)
                return

            } catch (error: Exception) {
                Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                if (attempt == 2) {
                    failed.addFirst { requestRange(count, offset, callback) }

                    if (error is VKApiExecutionException) {
                        val errorMsg = error.errorMsg ?: "Requesting photos is failed."
                        _loadMoreNetworkState.postValue(Error(errorMsg, error.code))
                    } else {
                        val errorMsg = error.message ?: "Requesting photos is failed."
                        _loadMoreNetworkState.postValue(Error(errorMsg))
                    }
                }
            }
        }
    }

    private suspend fun convertToPhotoItems(photos: List<Photo>): List<PhotoItem> {
        val deferredBitmaps = supervisorScope {
            photos.map { photo ->
                val url = photo.sizes["q"]?.url ?: photo.sizes["x"]?.url ?: photo.sizes["m"]?.url ?: return@map null

                async {
                    networkUtils.loadBitmapWithCaching(url)
                }
            }
        }

        return deferredBitmaps
            .map {
                it?.await()
            }
            .mapIndexed { index, bitmap ->
                photos[index].run {
                    val originalUrl = sizes["w"]?.url ?: sizes["z"]?.url ?: sizes["y"]?.url ?: sizes["x"]?.url
                    PhotoItem(id, bitmap, originalUrl, text, date, reposts, likes)
                }
            }
    }
}