package dev.iusupov.vkphotos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagedList
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.Repository
import dev.iusupov.vkphotos.utils.StorageUtils
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
import java.util.concurrent.Executor


class RepositoryTest {

    @Suppress("unused")
    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private lateinit var fakeApi: FakeApi
    private  lateinit var fakeStorageUtils: StorageUtils
    private val networkExecutor = Executor { command -> command.run() }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val repository = Repository(fakeApi, fakeStorageUtils, networkExecutor)

    @ObsoleteCoroutinesApi
    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(newSingleThreadContext("UI thread"))
    }

    @Test
    fun fetchFriends() {
        val listing = repository.fetchFriends(pageSize = PAGE_SIZE, coroutineScope = coroutineScope)
        val expected = fakeApi.users.take(20)
        val usersObserver = LoggingObserver<PagedList<User>>()
        val networkObserver = LoggingObserver<NetworkState>()

        listing.pagedList.observeForever(usersObserver)
        listing.loadMoreNetworkState.observeForever(networkObserver)

        assertThat(usersObserver.value, `is`(expected))
        assertThat(networkObserver.value?.state, `is`(State.SUCCESS))
    }

    @Test
    fun fetchFriendsWithRetry() {
        fakeApi.error = Exception(ERROR_MSG)
        val listing = repository.fetchFriends(pageSize = PAGE_SIZE, coroutineScope = coroutineScope)
        val expectedUsers = fakeApi.users.take(20)
        val usersObserver = LoggingObserver<PagedList<User>>()
        val networkObserver = LoggingObserver<NetworkState>()

        listing.pagedList.observeForever(usersObserver)
        listing.loadMoreNetworkState.observeForever(networkObserver)

        assertThat(usersObserver.value?.size, `is`(0))
        assertThat(networkObserver.value?.state, `is`(State.ERROR))
        assertThat(networkObserver.value?.error?.message, `is`(ERROR_MSG))

        fakeApi.error = null
        listing.retry()

        assertThat(usersObserver.value, `is`(expectedUsers))
        assertThat(networkObserver.value?.state, `is`(State.SUCCESS))
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val PAGE_SIZE = 10
    }
}