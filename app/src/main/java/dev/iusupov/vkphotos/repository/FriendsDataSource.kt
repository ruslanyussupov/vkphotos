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
import java.util.concurrent.LinkedBlockingQueue

class FriendsDataSource(private val request: Request,
                        private val coroutineScope: CoroutineScope) : PositionalDataSource<User>() {

    private val jobs = mutableSetOf<Job>()

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<User>) {
        if (!coroutineScope.isActive) return

        val count = params.requestedLoadSize
        val offset = 0

        jobs += coroutineScope.launch {
            request.requestInitial(count, offset, callback)
        }.apply {
            invokeOnCompletion {
                jobs.remove(this)
            }
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<User>) {
        if (!coroutineScope.isActive) return

        val count = params.loadSize
        val offset = params.startPosition

        jobs += coroutineScope.launch {
            request.requestRange(count, offset, callback)
        }.apply {
            invokeOnCompletion {
                jobs.remove(this)
            }
        }
    }

    override fun invalidate() {
        jobs.forEach {
            it.cancel()
        }
        super.invalidate()
    }

    class Request(private val userId: Int,
                  private val api: Api) {

        private val _requestInitialState = MutableLiveData<NetworkState>()
        private val _requestRangeState = MutableLiveData<NetworkState>()
        private val failed = LinkedBlockingQueue<suspend () -> Unit>()
        val requestInitialState: LiveData<NetworkState> = _requestInitialState
        val requestRangeState: LiveData<NetworkState> = _requestRangeState

        suspend fun requestInitial(count: Int, offset: Int, callback: LoadInitialCallback<User>) {
            Timber.d("Request initial: count=$count, offset=$offset")

            _requestInitialState.postValue(Loading)

            repeat(3) { attemptCount ->
                val delayInMillis = 2_000L * attemptCount
                delay(delayInMillis)

                try {
                    val response = api.fetchFriends(count, offset, userId)

                    Timber.i("Requesting friends completed: count=$count, offset=$offset, size=${response.users.size}")

                    callback.onResult(response.users, offset, response.count)

                    if (response.users.isEmpty()) {
                        _requestInitialState.postValue(
                            Error(
                                message = "There are no friends.",
                                code = Error.ERROR_CODE_NO_DATA
                            )
                        )
                    } else {
                        _requestInitialState.postValue(Loaded)
                    }

                    return

                } catch (error: Exception) {
                    Timber.e("Requesting friends is failed: count=$count, offset=$offset, attempt count=$attemptCount. $error")

                    if (attemptCount >= 2) {
                        failed.put { requestInitial(count, offset, callback) }

                        if (error is VKApiExecutionException) {
                            val message = error.errorMsg ?: "Requesting friends is failed."
                            _requestInitialState.postValue(Error(message, error.code))
                        } else {
                            val message = error.message ?: "Requesting friends is failed."
                            _requestInitialState.postValue(Error(message))
                        }
                    }
                }
            }
        }

        suspend fun requestRange(count: Int, offset: Int, callback: LoadRangeCallback<User>) {
            Timber.d("Request range: count=$count, offset=$offset")

            _requestRangeState.postValue(Loading)

            repeat(3) { attemptCount ->
                val delayInMillis = 2_000L * attemptCount
                delay(delayInMillis)

                try {
                    val response = api.fetchFriends(count, offset, userId)

                    Timber.i("Requesting friends completed: count=$count, offset=$offset, size=${response.users.size}")

                    callback.onResult(response.users)
                    _requestRangeState.postValue(Loaded)

                    return

                } catch (error: Exception) {
                    Timber.e("Requesting friends is failed: count=$count, offset=$offset, attempt count=$attemptCount. $error")

                    if (attemptCount >= 2) {
                        failed.put { requestRange(count, offset, callback) }

                        if (error is VKApiExecutionException) {
                            val message = error.errorMsg ?: "Requesting friends is failed."
                            _requestRangeState.postValue(Error(message, error.code))
                        } else {
                            val message = error.message ?: "Requesting friends is failed."
                            _requestRangeState.postValue(Error(message))
                        }
                    }
                }
            }
        }

        suspend fun retryAllFailed() {
            coroutineScope {
                while (failed.isNotEmpty()) {
                    val last = failed.take()
                    launch {
                        last()
                    }
                }
            }
        }
    }
}