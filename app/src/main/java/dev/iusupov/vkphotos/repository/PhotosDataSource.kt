package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.vk.api.sdk.exceptions.VKApiExecutionException
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.PhotoItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber

class PhotosDataSource(private val ownerId: Int,
                       private val api: Api,
                       private val coroutineScope: CoroutineScope) : PositionalDataSource<PhotoItem>() {

    private val _loadMoreNetworkState = MutableLiveData<NetworkState>()
    private val _loadInitialNetworkState = MutableLiveData<NetworkState>()
    val loadMoreNetworkState: LiveData<NetworkState> = _loadMoreNetworkState
    val loadInitialNetworkState: LiveData<NetworkState> = _loadInitialNetworkState
    var retry: (() -> Unit)? = null

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<PhotoItem>) {
        val count = params.requestedLoadSize
        val offset = params.requestedStartPosition
        requestInitial(count, offset, callback)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<PhotoItem>) {
        val count = params.loadSize
        val offset = params.startPosition
        requestRange(count, offset, callback)
    }

    private fun requestInitial(count: Int, offset: Int, callback: LoadInitialCallback<PhotoItem>) {
        if (!coroutineScope.isActive) return
        Timber.d("Request initial: count=$count, offset=$offset.")

        runBlocking {
            _loadInitialNetworkState.postValue(NetworkState.LOADING)

            coroutineScope.launch(Dispatchers.IO) {
                repeat(3) { attempt ->
                    val delayInMillis = 5_000L * attempt
                    delay(delayInMillis)

                    try {
                        val result = api.fetchPhotos(ownerId, count, offset)
                        Timber.i("Requesting photos completed: count=$count, offset=$offset, result=${result.photos.size}: ${result.photos}")
                        val photoItems = convertToPhotoItems(result.photos)
                        Timber.i("Converted to photo items: size=${photoItems.size}")
                        retry = null
                        callback.onResult(photoItems, offset, result.count)
                        if (photoItems.isEmpty()) {
                            _loadInitialNetworkState.postValue(NetworkState.error("No data.", ERROR_CODE_NO_DATA))
                        } else {
                            _loadInitialNetworkState.postValue(NetworkState.LOADED)
                        }
                        return@launch

                    } catch (error: VKApiExecutionException) {
                        Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                        val errorMsg = error.errorMsg ?: "Requesting photos is failed."

                        if (error.code == ERROR_CODE_PRIVATE_PROFILE) {
                            retry = { requestInitial(count, offset, callback) }
                            _loadInitialNetworkState.postValue(NetworkState.error(errorMsg, error.code))
                            return@launch
                        } else {
                            if (attempt == 2) {
                                retry = { requestInitial(count, offset, callback) }
                                _loadInitialNetworkState.postValue(NetworkState.error(errorMsg, error.code))
                            }
                        }

                    } catch (error: Exception) {
                        Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                        if (attempt == 2) {
                            retry = { requestInitial(count, offset, callback) }
                            val errorMsg = error.message ?: "Requesting photos is failed."
                            _loadInitialNetworkState.postValue(NetworkState.error(errorMsg))
                        }
                    }
                }
            }.join()
        }
    }

    private fun requestRange(count: Int, offset: Int, callback: LoadRangeCallback<PhotoItem>) {
        if (!coroutineScope.isActive) return
        Timber.i("Requesting range: count=$count, offset=$offset.")

        runBlocking {
            _loadMoreNetworkState.postValue(NetworkState.LOADING)

            coroutineScope.launch(Dispatchers.IO) {
                repeat(3) { attempt ->
                    val delayInMillis = 5_000L * attempt
                    delay(delayInMillis)

                    try {
                        val result = api.fetchPhotos(ownerId, count, offset)
                        Timber.i("Requesting photos completed: count=$count, offset=$offset, result=${result.photos.size}: ${result.photos}")
                        val photoItems = convertToPhotoItems(result.photos)
                        Timber.i("Converted to photo items: size=${photoItems.size}")
                        retry = null
                        callback.onResult(photoItems)
                        _loadMoreNetworkState.postValue(NetworkState.LOADED)
                        return@launch

                    } catch (error: Exception) {
                        Timber.e("Requesting photos is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                        if (attempt == 2) {
                            retry = { requestRange(count, offset, callback) }

                            if (error is VKApiExecutionException) {
                                val errorMsg = error.errorMsg ?: "Requesting photos is failed."
                                _loadMoreNetworkState.postValue(NetworkState.error(errorMsg, error.code))
                            } else {
                                val errorMsg = error.message ?: "Requesting photos is failed."
                                _loadMoreNetworkState.postValue(NetworkState.error(errorMsg))
                            }
                        }
                    }
                }
            }.join()
        }
    }

    private suspend fun convertToPhotoItems(photos: List<Photo>): List<PhotoItem> {
        val deferredBitmaps = photos.map { photo ->
            val url = photo.sizes["q"]?.url ?: photo.sizes["x"]?.url ?: photo.sizes["m"]?.url ?: return@map null

            parseUrlFromString(url)?.let {
                coroutineScope.loadBitmapAsync(it)
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