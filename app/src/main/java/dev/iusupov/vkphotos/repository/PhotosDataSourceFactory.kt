package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.PhotoItem
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class PhotosDataSourceFactory(private val ownerId: Int,
                              private val api: Api,
                              private val scope: CoroutineScope) : DataSource.Factory<Int, PhotoItem>() {

    private val _source = MutableLiveData<PhotosDataSource>()
    val source: LiveData<PhotosDataSource> = _source

    override fun create(): DataSource<Int, PhotoItem> {
        Timber.d("Creating photos positional data source...")
        val photosDataSource = PhotosDataSource(ownerId, api, scope)
        _source.postValue(photosDataSource)
        return photosDataSource
    }
}