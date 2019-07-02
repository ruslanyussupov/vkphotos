package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.utils.StorageUtils
import kotlinx.coroutines.CoroutineScope

class PhotosDataSourceFactory(private val ownerId: Int,
                              private val api: Api,
                              private val storageUtils: StorageUtils,
                              private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, PhotoItem>() {

    private val _source = MutableLiveData<PhotosDataSource>()
    val source: LiveData<PhotosDataSource> = _source

    override fun create(): DataSource<Int, PhotoItem> {
        val photosDataSource = PhotosDataSource(ownerId, api, storageUtils, coroutineScope)
        _source.postValue(photosDataSource)
        return photosDataSource
    }
}