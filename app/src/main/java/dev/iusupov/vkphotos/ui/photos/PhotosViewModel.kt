package dev.iusupov.vkphotos.ui.photos

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.loadBitmapAsync
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.parseUrlFromString
import dev.iusupov.vkphotos.repository.ApiImpl
import dev.iusupov.vkphotos.repository.DataSource
import dev.iusupov.vkphotos.repository.Repository
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.Exception

// TODO: use DI
class PhotosViewModel(ownerId: Int) : ViewModel() {

    private val dataSource: DataSource = Repository(ApiImpl(), Executors.newFixedThreadPool(5))
    val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _openedPhoto = MutableLiveData<Bitmap>()
    val openedPhoto: LiveData<Bitmap> = _openedPhoto

    private val _openedPhotoState = MutableLiveData<NetworkState>()
    val openedPhotoState: LiveData<NetworkState> = _openedPhotoState

    val photosListing by lazy {
        dataSource.fetchPhotos(ownerId = ownerId, coroutineScope = viewModelScope)
    }

    val isLoading = ObservableBoolean()
    val stateText = MutableLiveData<String?>()

    fun loadOpenedPhoto(photoItem: PhotoItem) {
        _openedPhoto.value = photoItem.thumbnail
        _openedPhotoState.value = NetworkState.LOADING

        val url = photoItem.originalUrl

        if (url == null) {
            _openedPhoto.postValue(photoItem.thumbnail)
            _openedPhotoState.value = NetworkState.LOADED
        } else {
            parseUrlFromString(url)?.let {
                try {
                    viewModelScope.launch {
                        val result = loadBitmapAsync(it).await()
                        _openedPhoto.postValue(result)
                        _openedPhotoState.postValue(NetworkState.LOADED)
                    }
                } catch (error: Exception) {
                    val message = error.message ?: "Something went wrong."
                    _openedPhotoState.postValue(NetworkState.error(message))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("#onCleared")
        viewModelScope.cancel()
    }
}