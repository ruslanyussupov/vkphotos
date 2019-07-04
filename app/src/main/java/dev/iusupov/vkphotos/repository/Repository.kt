package dev.iusupov.vkphotos.repository

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor


class Repository(private val api: Api,
                 private val networkUtils: NetworkUtils,
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
        val factory = FriendsDataSourceFactory(userId, api, coroutineScope)
        val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()
        val loadInitialNetworkState = Transformations.switchMap(factory.source) { it.loadInitialNetworkState }
        val loadMoreNetworkState = Transformations.switchMap(factory.source) { it.loadMoreNetworkState }
        val retry = Transformations.map(factory.source) { it.retryFailed }

        return Listing(pagedList, loadInitialNetworkState, loadMoreNetworkState, retry)
    }

    override fun fetchPhotos(
        ownerId: Int,
        coroutineScope: CoroutineScope,
        pageSize: Int
    ): Listing<PhotoItem> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .build()
        val factory = PhotosDataSourceFactory(ownerId, api, networkUtils, coroutineScope)
        val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()
        val loadInitialNetworkState = Transformations.switchMap(factory.source) { it.loadInitialNetworkState }
        val loadMoreNetworkState = Transformations.switchMap(factory.source) { it.loadMoreNetworkState }
        val retry = Transformations.map(factory.source) { it.retryFailed }

        return Listing(pagedList, loadInitialNetworkState, loadMoreNetworkState, retry)
    }
}