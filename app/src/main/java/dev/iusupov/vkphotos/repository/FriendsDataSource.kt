package dev.iusupov.vkphotos.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PositionalDataSource
import com.vk.api.sdk.exceptions.VKApiExecutionException
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.model.User
import kotlinx.coroutines.*
import timber.log.Timber


class FriendsDataSource(private val userId: Int = -1,
                        private val api: Api,
                        private val coroutineScope: CoroutineScope) : PositionalDataSource<User>() {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState> = _networkState
    var retry: (() -> Unit)? = null

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
            _networkState.postValue(NetworkState.LOADING)

            coroutineScope.launch(Dispatchers.IO) {
                repeat(3) { attempt ->
                    try {
                        val result = api.fetchFriends(userId, count, offset)
                        Timber.i("Requesting friends completed. #${result.users.size} ${result.users}")
                        retry = null
                        callback.onResult(result.users, offset, result.count)
                        _networkState.postValue(NetworkState.LOADED)
                        return@launch

                    } catch(e: Exception) {
                        Timber.e("Requesting friends is failed #$attempt. $e")
                        if (attempt == 2) {
                            retry = {
                                requestInitial(count, offset, callback)
                            }
                            val errorMsg =
                                if (e is VKApiExecutionException) {
                                    e.errorMsg
                                } else {
                                    e.message
                                }
                            _networkState.postValue(NetworkState.error(errorMsg?: "Requesting friends is failed."))
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
            _networkState.postValue(NetworkState.LOADING)

            coroutineScope.launch(Dispatchers.IO) {
                repeat(3) { attempt ->
                    try {
                        val result = api.fetchFriends(userId, count, offset)
                        Timber.i("Requesting friends completed. Offset=$offset #${result.users.size} ${result.users}")
                        retry = null
                        callback.onResult(result.users)
                        _networkState.postValue(NetworkState.LOADED)
                        return@launch

                    } catch (e: Exception) {
                        Timber.e("Requesting friends is failed #$attempt. $e")
                        if (attempt == 2) {
                            retry = {
                                requestRange(count, offset, callback)
                            }
                            val errorMsg =
                                if (e is VKApiExecutionException) {
                                    e.errorMsg
                                } else {
                                    e.message
                                }
                            _networkState.postValue(NetworkState.error(errorMsg?: "Requesting friends is failed."))
                        }
                    }
                }
            }.join()
        }
    }
}