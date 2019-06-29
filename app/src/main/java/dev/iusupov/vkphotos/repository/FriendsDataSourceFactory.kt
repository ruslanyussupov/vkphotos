package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class FriendsDataSourceFactory(private val userId: Int = -1,
                               private val api: Api,
                               private val coroutineScope: CoroutineScope) : DataSource.Factory<Int, User>() {

    private val _source = MutableLiveData<FriendsDataSource>()
    val source: LiveData<FriendsDataSource> = _source

    override fun create(): DataSource<Int, User> {
        Timber.d("Creating friends positional data source...")
        val friendsDataSource = FriendsDataSource(userId, api, coroutineScope)
        _source.postValue(friendsDataSource)
        return friendsDataSource
    }
}