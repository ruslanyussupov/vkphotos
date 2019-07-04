package dev.iusupov.vkphotos


// TODO: This shit doesn't work. Fix it.
/*
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagedList
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.Repository
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
import java.util.concurrent.Executor

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class RepositoryTest {

    @Suppress("unused")
    @get:Rule // used to make all live data calls sync
    val instantExecutor = InstantTaskExecutorRule()

    private val fakeApi = FakeApi()
    private val networkUtils = NetworkUtils(storageUtils = FakeStorageUtils())
    private val networkExecutor = Executor { command -> command.run() }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val testCoroutineDispatcher = TestCoroutineDispatcher()
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    private val repository = Repository(fakeApi, networkUtils, networkExecutor)

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
        networkUtils.swapNetworkDispatcher(testCoroutineDispatcher)
        networkUtils.swapComputationDispatcher(testCoroutineDispatcher)
    }

    @Test
    fun fetchFriends() = runBlocking {
        fakeApi.error = null

        val listing = repository.fetchFriends(
            pageSize = PAGE_SIZE,
            coroutineScope = coroutineScope,
            networkDispatcher = testCoroutineDispatcher)

        val usersObserver = LoggingObserver<PagedList<User>>()
        val networkObserver = LoggingObserver<NetworkState>()

        listing.pagedList.observeForever(usersObserver)
        listing.loadInitialNetworkState.observeForever(networkObserver)

        delay(100L)

        assertThat(networkObserver.value?.state, `is`(State.SUCCESS))
        assert(usersObserver.value?.isNullOrEmpty() == false)
    }

    @Test
    fun fetchFriendsWithRetry() = runBlockingTest {
        fakeApi.error = Exception(ERROR_MSG)

        val listing = repository.fetchFriends(
            pageSize = PAGE_SIZE,
            coroutineScope = coroutineScope,
            networkDispatcher = testCoroutineDispatcher)

        val expectedUsers = fakeApi.users.take(20)
        val usersObserver = LoggingObserver<PagedList<User>>()
        val networkStateObserver = LoggingObserver<NetworkState>()
        val retryObserver = LoggingObserver<() -> Unit>()

        listing.pagedList.observeForever(usersObserver)
        listing.loadInitialNetworkState.observeForever(networkStateObserver)
        listing.retry.observeForever(retryObserver)

        assertThat(usersObserver.value?.size, `is`(0))
        assertThat(networkStateObserver.value?.state, `is`(State.ERROR))
        assertThat(networkStateObserver.value?.error?.message, `is`(ERROR_MSG))

        fakeApi.error = null
        retryObserver.value?.invoke()

        assertThat(usersObserver.value, `is`(expectedUsers))
        assertThat(networkStateObserver.value?.state, `is`(State.SUCCESS))
    }

    companion object {
        private const val ERROR_MSG = "Something went wrong..."
        private const val PAGE_SIZE = 10
    }
}
*/
