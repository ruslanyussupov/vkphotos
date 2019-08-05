package dev.iusupov.vkphotos.repository

import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.CoroutineScope

class FriendsDataSourceFactory(private val request: FriendsDataSource.Request,
                               private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, User>() {

    private var dataSource: FriendsDataSource? = null

    override fun create(): DataSource<Int, User> {
        dataSource = FriendsDataSource(request, coroutineScope)
        return dataSource as FriendsDataSource
    }

    fun invalidateDataSource() {
        dataSource?.invalidate()
    }
}