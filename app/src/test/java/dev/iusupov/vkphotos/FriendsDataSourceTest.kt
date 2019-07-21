package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PositionalDataSource
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.FriendsDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class FriendsDataSourceTest {

    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val fakeApi = FakeApi()

    private val dataSource = FriendsDataSource(
        userId = -1,
        api = fakeApi,
        coroutineScope = coroutineScope)

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun requestInitial() = runBlockingTest {
        fakeApi.error = null

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

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRange() = runBlockingTest {
        fakeApi.error = null

        val offset = 20
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.requestRange(LOAD_SIZE, offset, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                TODO("Isn't supposed to be called.")
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestInitial(LOAD_SIZE, 0, callback)
        dataSource.failed.clear()

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))
    }

    @Test
    fun requestRangeWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestRange(LOAD_SIZE, 20, callback)
        dataSource.failed.clear()

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))
    }

    @Test
    fun requestInitialAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

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

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRangeAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val offset = 20
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.requestRange(LOAD_SIZE, offset, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

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

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        var state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))

        fakeApi.error = null

        while (dataSource.failed.isNotEmpty()) {
            dataSource.failed.removeLast().invoke()
        }

        state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestRangeWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val offset = 20
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(offset, offset + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.requestRange(LOAD_SIZE, offset, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        var state = networkStateObserver.value

        assert(state is Error)
        state as Error
        assertThat(state.message, `is`(ERROR_MSG))

        fakeApi.error = null

        while (dataSource.failed.isNotEmpty()) {
            dataSource.failed.removeLast().invoke()
        }
        state = networkStateObserver.value

        assert(state is Loaded)
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val LOAD_SIZE = 20
    }
}