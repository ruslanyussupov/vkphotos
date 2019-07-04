package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class PhotosDataSourceFactory(private val ownerId: Int,
                              private val api: Api,
                              private val networkUtils: NetworkUtils,
                              private val coroutineScope: CoroutineScope,
                              private val networkDispatcher: CoroutineDispatcher) : DataSource.Factory<Int, PhotoItem>() {

    private val _source = MutableLiveData<PhotosDataSource>()
    val source: LiveData<PhotosDataSource> = _source

    override fun create(): DataSource<Int, PhotoItem> {
        val photosDataSource = PhotosDataSource(ownerId, api, networkUtils, coroutineScope, networkDispatcher)
        _source.postValue(photosDataSource)
        return photosDataSource
    }
}