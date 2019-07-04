package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class FriendsDataSourceFactory(private val userId: Int = -1,
                               private val api: Api,
                               private val coroutineScope: CoroutineScope,
                               private val networkDispatcher: CoroutineDispatcher) : DataSource.Factory<Int, User>() {

    private val _source = MutableLiveData<FriendsDataSource>()
    val source: LiveData<FriendsDataSource> = _source

    override fun create(): DataSource<Int, User> {
        val friendsDataSource = FriendsDataSource(userId, api, coroutineScope, networkDispatcher)
        _source.postValue(friendsDataSource)
        return friendsDataSource
    }
}