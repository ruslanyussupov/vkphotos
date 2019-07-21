package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.vk.api.sdk.exceptions.VKApiExecutionException
import dev.iusupov.vkphotos.Error
import dev.iusupov.vkphotos.Loaded
import dev.iusupov.vkphotos.Loading
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.LinkedList

// TODO: Maybe it's better to move requesting into independent class
class FriendsDataSource(private val userId: Int,
                        private val api: Api,
                        private val coroutineScope: CoroutineScope) : PositionalDataSource<User>() {

    private val _loadMoreNetworkState = MutableLiveData<NetworkState>()
    private val _loadInitialNetworkState = MutableLiveData<NetworkState>()
    val failed = LinkedList<suspend () -> Unit>()
    val loadMoreNetworkState: LiveData<NetworkState> = _loadMoreNetworkState
    val loadInitialNetworkState: LiveData<NetworkState> = _loadInitialNetworkState

    val retryFailed = {
        while (failed.isNotEmpty()) {
            val last = failed.removeLast()
            coroutineScope.launch {
                last.invoke()
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<User>) {
        val count = params.requestedLoadSize
        val offset = params.requestedStartPosition

        coroutineScope.launch {
            requestInitial(count, offset, callback)
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<User>) {
        val count = params.loadSize
        val offset = params.startPosition

        coroutineScope.launch {
            requestRange(count, offset, callback)
        }
    }

    suspend fun requestInitial(count: Int, offset: Int, callback: LoadInitialCallback<User>) {
        if (!coroutineScope.isActive) return
        Timber.d("Request initial: count=$count, offset=$offset")

        _loadInitialNetworkState.postValue(Loading)

        repeat(3) { attempt ->
            val delayInMillis = (2_000L * attempt)
            delay(delayInMillis)

            try {
                val result = api.fetchFriends(count, offset, userId)
                Timber.i("Requesting friends completed: count=$count, offset=$offset, result=${result.users.size}: ${result.users}")
                callback.onResult(result.users, offset, result.count)
                if (result.users.isEmpty()) {
                    _loadInitialNetworkState.postValue(
                        Error(
                            message = "No data.",
                            code = Error.ERROR_CODE_NO_DATA
                        )
                    )
                } else {
                    _loadInitialNetworkState.postValue(Loaded)
                }
                return

            } catch(error: Exception) {
                Timber.e("Requesting friends is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                if (attempt == 2) {
                    failed.addFirst { requestInitial(count, offset, callback) }

                    if (error is VKApiExecutionException) {
                        val errorMsg = error.errorMsg ?: "Requesting friends is failed."
                        _loadInitialNetworkState.postValue(Error(errorMsg, error.code))
                    } else {
                        val errorMsg = error.message ?: "Requesting friends is failed."
                        _loadInitialNetworkState.postValue(Error(errorMsg))
                    }
                }
            }
        }
    }

    suspend fun requestRange(count: Int, offset: Int, callback: LoadRangeCallback<User>) {
        if (!coroutineScope.isActive) return
        Timber.d("Request range: count=$count, offset=$offset")

        _loadMoreNetworkState.postValue(Loading)

        repeat(3) { attempt ->
            val delayInMillis = 2_000L * attempt
            delay(delayInMillis)

            try {
                val result = api.fetchFriends(count, offset, userId)
                Timber.i("Requesting friends completed: count=$count, offset=$offset, result=${result.users.size}: ${result.users}")
                callback.onResult(result.users)
                _loadMoreNetworkState.postValue(Loaded)
                return

            } catch (error: Exception) {
                Timber.e("Requesting friends is failed count=$count, offset=$offset, attempt=$attempt. $error")

                if (attempt == 2) {
                    failed.addFirst { requestRange(count, offset, callback) }

                    if (error is VKApiExecutionException) {
                        val errorMsg = error.errorMsg ?: "Requesting friends is failed."
                        _loadMoreNetworkState.postValue(Error(errorMsg, error.code))
                    } else {
                        val errorMsg = error.message ?: "Requesting friends is failed."
                        _loadMoreNetworkState.postValue(Error(errorMsg))
                    }
                }
            }
        }
    }
}