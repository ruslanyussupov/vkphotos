package dev.iusupov.vkphotos.ui.photos

import android.graphics.Bitmap
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.iusupov.vkphotos.App
import dev.iusupov.vkphotos.Error
import dev.iusupov.vkphotos.Loaded
import dev.iusupov.vkphotos.Loading
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.repository.DataSource
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import timber.log.Timber
import javax.inject.Inject


class PhotosViewModel(ownerId: Int) : ViewModel() {

    @Inject lateinit var dataSource: DataSource
    @Inject lateinit var networkUtils: NetworkUtils

    val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _openedPhoto = MutableLiveData<Bitmap>()
    val openedPhoto: LiveData<Bitmap> = _openedPhoto

    private val _openedPhotoState = MutableLiveData<NetworkState>()
    val openedPhotoState: LiveData<NetworkState> = _openedPhotoState

    val photosListing by lazy {
        dataSource.fetchPhotos(
            ownerId = ownerId,
            coroutineScope = viewModelScope
        )
    }

    val isLoading = ObservableBoolean()
    val stateText = MutableLiveData<String?>()

    init {
        App.dataComponent.inject(this)
    }

    fun loadOpenedPhoto(photo: Photo, thumbnail: Bitmap?) {
        _openedPhoto.value = thumbnail
        _openedPhotoState.value = Loading

        val url = photo.run {
            sizes["w"]?.url ?: sizes["z"]?.url ?: sizes["y"]?.url ?: sizes["x"]?.url
        }

        if (url == null) {
            _openedPhotoState.value = Loaded
        } else {
            viewModelScope.launch {
                val bitmap = networkUtils.loadBitmap(url)
                if (bitmap == null) {
                    _openedPhotoState.postValue(Error("Can't load image."))
                } else {
                    _openedPhoto.postValue(bitmap)
                    _openedPhotoState.postValue(Loaded)
                }
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            photosListing.retry()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("#onCleared")
        viewModelScope.cancel()
    }
}