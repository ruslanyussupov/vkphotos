package dev.iusupov.vkphotos.repository

import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.Photo
import kotlinx.coroutines.CoroutineScope

class PhotosDataSourceFactory(private val request: PhotosDataSource.Request,
                              private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, Photo>() {

    override fun create(): DataSource<Int, Photo> {
        return PhotosDataSource(request, coroutineScope)
    }
}