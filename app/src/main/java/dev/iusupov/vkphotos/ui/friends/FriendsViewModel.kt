package dev.iusupov.vkphotos.ui.friends

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.iusupov.vkphotos.App
import dev.iusupov.vkphotos.repository.DataSource
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class FriendsViewModel : ViewModel() {

    @Inject lateinit var dataSource: DataSource
    @Inject lateinit var networkUtils: NetworkUtils

    val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        App.dataComponent.inject(this)
    }

    val friendsListing by lazy {
        dataSource.fetchFriends(viewModelScope)
    }

    val isLoading =  ObservableBoolean()
    val stateText = MutableLiveData<String>()

    fun retry() {
        viewModelScope.launch {
            friendsListing.retry()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("#onCleared")
        viewModelScope.cancel()
    }
}