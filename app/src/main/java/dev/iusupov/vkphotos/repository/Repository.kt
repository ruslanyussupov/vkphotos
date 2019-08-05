package dev.iusupov.vkphotos.repository

import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor


class Repository(private val api: Api,
                 private val executor: Executor) : DataSource {

    override fun fetchFriends(
        coroutineScope: CoroutineScope,
        userId: Int,
        pageSize: Int
    ): Listing<User> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .build()
        val request = FriendsDataSource.Request(userId, api)
        val factory = FriendsDataSourceFactory(request, coroutineScope)
        val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()

        return Listing(
            pagedList,
            request.requestInitialState,
            request.requestRangeState,
            request::retryAllFailed,
            factory::invalidateDataSource
        )
    }

    override fun fetchPhotos(
        ownerId: Int,
        coroutineScope: CoroutineScope,
        pageSize: Int
    ): Listing<Photo> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .build()
        val request = PhotosDataSource.Request(ownerId, api)
        val factory = PhotosDataSourceFactory(request, coroutineScope)
        val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()

        return Listing(
            pagedList,
            request.requestInitialState,
            request.requestRangeState,
            request::retryAllFailed,
            factory::invalidateDataSource
        )
    }
}