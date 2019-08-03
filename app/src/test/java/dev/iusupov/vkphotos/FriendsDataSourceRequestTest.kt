package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PositionalDataSource
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.FriendsDataSource
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FriendsDataSourceRequestTest {

    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val fakeApi = FakeApi()

    @Test
    fun requestInitial() = runBlockingTest {
        fakeApi.error = null

        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        request.requestInitial(LOAD_SIZE, 0, callback)

        request.requestInitialState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRange() = runBlockingTest {
        fakeApi.error = null

        val offset = 20
        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        request.requestRange(LOAD_SIZE, offset, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                TODO("Isn't supposed to be called.")
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        request.requestInitial(LOAD_SIZE, 0, callback)

        request.requestInitialState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))
    }

    @Test
    fun requestRangeWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                TODO("Isn't supposed to be called.")
            }
        }

        request.requestRange(LOAD_SIZE, 20, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))
    }

    @Test
    fun requestInitialAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(20)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        request.requestInitial(LOAD_SIZE, 0, callback)

        request.requestInitialState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRangeAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val offset = 20
        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        request.requestRange(LOAD_SIZE, offset, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(20)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        request.requestInitial(LOAD_SIZE, 0, callback)

        request.requestInitialState.observeForever(networkStateObserver)
        var state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))

        fakeApi.error = null

        request.retryAllFailed()

        state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRangeWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val offset = 20
        val request = FriendsDataSource.Request(-1,  fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        request.requestRange(LOAD_SIZE, offset, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        var state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))

        fakeApi.error = null

        request.retryAllFailed()

        state = networkStateObserver.value

        assert(state is Loaded)
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val LOAD_SIZE = 20
    }
}