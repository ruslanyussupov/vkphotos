package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PositionalDataSource
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.repository.PhotosDataSource
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PhotosDataSourceTest {

    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val fakeApi = FakeApi()
    private val networkUtils = NetworkUtils(FakeStorageUtils())
    private val dataSource = PhotosDataSource(
        ownerId = 1,
        api = fakeApi,
        networkUtils = networkUtils,
        coroutineScope = coroutineScope)

    @Before
    fun setup() {
        Dispatchers.setMain(mainThreadSurrogate)
        networkUtils.swapNetworkDispatcher(testCoroutineDispatcher)
        networkUtils.swapComputationDispatcher(testCoroutineDispatcher)
    }

    @Test
    fun requestInitial() = runBlockingTest {
        fakeApi.error = null

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<PhotoItem>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun requestRange() = runBlockingTest {
        fakeApi.error = null

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<PhotoItem>(){
            override fun onResult(data: MutableList<PhotoItem>) {
                assert(!data.isNullOrEmpty())
            }
        }

        dataSource.requestRange(LOAD_SIZE, 20, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun requestInitialWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>, position: Int, totalCount: Int) {
                assert(data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<PhotoItem>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestInitial(LOAD_SIZE, 0, callback)
        dataSource.failed.clear()

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state
        val errorMsg = networkStateObserver.value?.error?.message

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))
    }

    @Test
    fun requestRangeWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>) {
                assert(data.isNullOrEmpty())
            }
        }

        dataSource.requestRange(LOAD_SIZE, 20, callback)
        dataSource.failed.clear()

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state
        val errorMsg = networkStateObserver.value?.error?.message

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))
    }

    @Test
    fun requestInitialAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<PhotoItem>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun requestRangeAfterRecovery() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>) {
                assert(!data.isNullOrEmpty())
            }
        }

        dataSource.requestRange(LOAD_SIZE, 0, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        val state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun requestInitialWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<PhotoItem>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.requestInitial(LOAD_SIZE, 0, callback)

        dataSource.loadInitialNetworkState.observeForever(networkStateObserver)
        var state = networkStateObserver.value?.state
        val errorMsg = networkStateObserver.value?.error?.message

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))

        fakeApi.error = null

        while (dataSource.failed.isNotEmpty()) {
            dataSource.failed.removeLast().invoke()
        }

        state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun requestRangeWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<PhotoItem>() {
            override fun onResult(data: MutableList<PhotoItem>) {
                assert(!data.isNullOrEmpty())
            }
        }

        dataSource.requestRange(LOAD_SIZE, 20, callback)

        dataSource.loadMoreNetworkState.observeForever(networkStateObserver)
        var state = networkStateObserver.value?.state
        val errorMsg = networkStateObserver.value?.error?.message

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))

        fakeApi.error = null

        while (dataSource.failed.isNotEmpty()) {
            dataSource.failed.removeLast().invoke()
        }

        state = networkStateObserver.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val LOAD_SIZE = 20
    }
}