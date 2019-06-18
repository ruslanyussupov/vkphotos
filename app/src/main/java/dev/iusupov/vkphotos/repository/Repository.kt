package dev.iusupov.vkphotos.repository

import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.Listing
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.Executor

class Repository(private val api: Api,
                 private val executor: Executor) : DataSource {

    override fun fetchFriends(userId: Int, pageSize: Int, coroutineScope: CoroutineScope): Listing<User> {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(pageSize)
            .setInitialLoadSizeHint(pageSize * 2)
            .build()
        val factory = FriendsDataSourceFactory(userId, api, coroutineScope)
        val pagedList = LivePagedListBuilder(factory, config).setFetchExecutor(executor).build()
        val networkState = Transformations.switchMap(factory.source) { it.networkState }
        val retry: () -> Unit = { factory.source.value?.retry?.invoke() }
        return Listing(pagedList, networkState, retry)
    }

    override fun getPhotos(ownerId: Int) {
        TODO("not implemented")
    }
}