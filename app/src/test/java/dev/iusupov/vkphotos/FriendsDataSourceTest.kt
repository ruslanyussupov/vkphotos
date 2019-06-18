package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PositionalDataSource
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.FriendsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FriendsDataSourceTest {

    @Suppress("unused")
    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val fakeApi = FakeApi()
    private val dataSource = FriendsDataSource(api = fakeApi, coroutineScope = CoroutineScope(Dispatchers.Main))

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(newSingleThreadContext("UI thread"))
    }

    @Test
    fun loadInitial() {
        fakeApi.error = null

        val params = PositionalDataSource.LoadInitialParams(0, LOAD_SIZE, PAGE_SIZE, false)
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(20)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.loadInitial(params, callback)

        val state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun loadRange() {
        fakeApi.error = null

        val params = PositionalDataSource.LoadRangeParams(20, LOAD_SIZE)
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(20, 20 + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.loadRange(params, callback)

        val state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun loadInitialWithError() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val params = PositionalDataSource.LoadInitialParams(0, LOAD_SIZE, PAGE_SIZE, false)
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                TODO("Isn't supposed to be called.")
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.loadInitial(params, callback)

        val state = dataSource.networkState.value?.state
        val errorMsg = dataSource.networkState.value?.errorMsg

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))
    }

    @Test
    fun loadRangeWithError() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val params = PositionalDataSource.LoadRangeParams(20, LOAD_SIZE)
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.loadRange(params, callback)

        val state = dataSource.networkState.value?.state
        val errorMsg = dataSource.networkState.value?.errorMsg

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))
    }

    @Test
    fun loadInitialAfterRecovery() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val params = PositionalDataSource.LoadInitialParams(0, LOAD_SIZE, PAGE_SIZE, false)
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(20)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.loadInitial(params, callback)

        val state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun loadRangeAfterRecovery() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = true

        val params = PositionalDataSource.LoadRangeParams(20, LOAD_SIZE)
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(20, 20 + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.loadRange(params, callback)

        val state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun loadInitialWithRetry() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val params = PositionalDataSource.LoadInitialParams(0, LOAD_SIZE, PAGE_SIZE, false)
        val callback = object : PositionalDataSource.LoadInitialCallback<User>() {
            override fun onResult(data: MutableList<User>, position: Int, totalCount: Int) {
                val expectedResult = fakeApi.users.take(20)
                assertThat(data, `is`(expectedResult))
            }

            override fun onResult(data: MutableList<User>, position: Int) {
                TODO("Isn't supposed to be called.")
            }
        }

        dataSource.loadInitial(params, callback)

        var state = dataSource.networkState.value?.state
        val errorMsg = dataSource.networkState.value?.errorMsg

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))

        fakeApi.error = null
        dataSource.retry?.invoke()

        state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    @Test
    fun loadRangeWithRetry() {
        fakeApi.error = Exception(ERROR_MSG)
        fakeApi.withRecovery = false

        val params = PositionalDataSource.LoadRangeParams(20, LOAD_SIZE)
        val callback = object : PositionalDataSource.LoadRangeCallback<User>() {
            override fun onResult(data: MutableList<User>) {
                val expectedResult = fakeApi.users.subList(20, 20 + LOAD_SIZE)
                assertThat(data, `is`(expectedResult))
            }
        }

        dataSource.loadRange(params, callback)

        var state = dataSource.networkState.value?.state
        val errorMsg = dataSource.networkState.value?.errorMsg

        assertThat(state, `is`(State.ERROR))
        assertThat(errorMsg, `is`(ERROR_MSG))

        fakeApi.error = null
        dataSource.retry?.invoke()

        state = dataSource.networkState.value?.state

        assertThat(state, `is`(State.SUCCESS))
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val PAGE_SIZE = 10
        private const val LOAD_SIZE = 20
    }
}