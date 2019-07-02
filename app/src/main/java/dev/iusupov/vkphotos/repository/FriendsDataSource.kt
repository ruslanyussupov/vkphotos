package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.vk.api.sdk.exceptions.VKApiExecutionException
import dev.iusupov.vkphotos.vksdk.ERROR_CODE_NO_DATA
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import timber.log.Timber


class FriendsDataSource(private val userId: Int = -1,
                        private val api: Api,
                        private val coroutineScope: CoroutineScope) : PositionalDataSource<User>() {

    private val _loadMoreNetworkState = MutableLiveData<NetworkState>()
    private val _loadInitialNetworkState = MutableLiveData<NetworkState>()
    private var retry: (() -> Unit)? = null
    val loadMoreNetworkState: LiveData<NetworkState> = _loadMoreNetworkState
    val loadInitialNetworkState: LiveData<NetworkState> = _loadInitialNetworkState

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<User>) {
        val count = params.requestedLoadSize
        val offset = params.requestedStartPosition
        requestInitial(count, offset, callback)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<User>) {
        val count = params.loadSize
        val offset = params.startPosition
        requestRange(count, offset, callback)
    }

    private fun requestInitial(count: Int, offset: Int, callback: LoadInitialCallback<User>) {
        if (!coroutineScope.isActive) return
        Timber.d("Request initial: count=$count, offset=$offset")

        runBlocking {
            _loadInitialNetworkState.postValue(NetworkState.LOADING)

            coroutineScope.launch {
                repeat(3) { attempt ->
                    val delayInMillis = (5_000L * attempt)
                    delay(delayInMillis)

                    try {
                        val result = api.fetchFriends(userId, count, offset)
                        Timber.i("Requesting friends completed: count=$count, offset=$offset, result=${result.users.size}: ${result.users}")
                        retry = null
                        callback.onResult(result.users, offset, result.count)
                        if (result.users.isEmpty()) {
                            _loadInitialNetworkState.postValue(NetworkState.error("No data.",
                                ERROR_CODE_NO_DATA
                            ))
                        } else {
                            _loadInitialNetworkState.postValue(NetworkState.LOADED)
                        }
                        return@launch

                    } catch(error: Exception) {
                        Timber.e("Requesting friends is failed: count=$count, offset=$offset, attempt=$attempt. $error")

                        if (attempt == 2) {
                            retry = { requestInitial(count, offset, callback) }

                            if (error is VKApiExecutionException) {
                                val errorMsg = error.errorMsg ?: "Requesting friends is failed."
                                _loadInitialNetworkState.postValue(NetworkState.error(errorMsg, error.code))
                            } else {
                                val errorMsg = error.message ?: "Requesting friends is failed."
                                _loadInitialNetworkState.postValue(NetworkState.error(errorMsg))
                            }
                        }
                    }
                }
            }.join()
        }
    }

    private fun requestRange(count: Int, offset: Int, callback: LoadRangeCallback<User>) {
        if (!coroutineScope.isActive) return
        Timber.d("Request range: count=$count, offset=$offset")

        runBlocking {
            _loadMoreNetworkState.postValue(NetworkState.LOADING)

            coroutineScope.launch {
                repeat(3) { attempt ->
                    val delayInMillis = 5_000L * attempt
                    delay(delayInMillis)

                    try {
                        val result = api.fetchFriends(userId, count, offset)
                        Timber.i("Requesting friends completed: count=$count, offset=$offset, result=${result.users.size}: ${result.users}")
                        retry = null
                        callback.onResult(result.users)
                        _loadMoreNetworkState.postValue(NetworkState.LOADED)
                        return@launch

                    } catch (error: Exception) {
                        Timber.e("Requesting friends is failed count=$count, offset=$offset, attempt=$attempt. $error")

                        if (attempt == 2) {
                            retry = { requestRange(count, offset, callback) }

                            if (error is VKApiExecutionException) {
                                val errorMsg = error.errorMsg ?: "Requesting friends is failed."
                                _loadMoreNetworkState.postValue(NetworkState.error(errorMsg, error.code))
                            } else {
                                val errorMsg = error.message ?: "Requesting friends is failed."
                                _loadMoreNetworkState.postValue(NetworkState.error(errorMsg))
                            }
                        }
                    }
                }
            }.join()
        }
    }

    fun retryFailed() {
        retry?.invoke()
    }
}