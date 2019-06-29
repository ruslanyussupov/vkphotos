package dev.iusupov.vkphotos.ui.friends

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.iusupov.vkphotos.repository.ApiImpl
import dev.iusupov.vkphotos.repository.DataSource
import dev.iusupov.vkphotos.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import timber.log.Timber
import java.util.concurrent.Executors

// TODO: use DI
class FriendsViewModel : ViewModel() {

    private val dataSource: DataSource = Repository(ApiImpl(), Executors.newFixedThreadPool(5))
    val viewModelScope = CoroutineScope(Dispatchers.Main)

    val friendsListing by lazy {
        dataSource.fetchFriends(userId = 6492, coroutineScope = viewModelScope)
    }

    val isLoading =  ObservableBoolean()
    val stateText = MutableLiveData<String>()

    override fun onCleared() {
        super.onCleared()
        Timber.d("#onCleared")
        viewModelScope.cancel()
    }
}