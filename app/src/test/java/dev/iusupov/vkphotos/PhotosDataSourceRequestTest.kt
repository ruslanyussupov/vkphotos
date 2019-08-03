package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PositionalDataSource
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.repository.PhotosDataSource
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PhotosDataSourceRequestTest {

    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val fakeApi = FakeApi()

    @Test
    fun requestInitial() = runBlockingTest {
        fakeApi.error = null

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<Photo>, position: Int) {
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

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<Photo>(){
            override fun onResult(data: MutableList<Photo>) {
                assert(!data.isNullOrEmpty())
            }
        }

        request.requestRange(LOAD_SIZE, 20, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithError() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>, position: Int, totalCount: Int) {
                assert(data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<Photo>, position: Int) {
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

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>) {
                assert(data.isNullOrEmpty())
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

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<Photo>, position: Int) {
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

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>) {
                assert(!data.isNullOrEmpty())
            }
        }

        request.requestRange(LOAD_SIZE, 0, callback)

        request.requestRangeState.observeForever(networkStateObserver)
        val state = networkStateObserver.value

        assert(state is Loaded)
    }

    @Test
    fun requestInitialWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadInitialCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>, position: Int, totalCount: Int) {
                assert(!data.isNullOrEmpty())
            }

            override fun onResult(data: MutableList<Photo>, position: Int) {
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

        val request = PhotosDataSource.Request(1, fakeApi)
        val networkStateObserver = LoggingObserver<NetworkState>()
        val callback = object : PositionalDataSource.LoadRangeCallback<Photo>() {
            override fun onResult(data: MutableList<Photo>) {
                assert(!data.isNullOrEmpty())
            }
        }

        request.requestRange(LOAD_SIZE, 20, callback)

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
