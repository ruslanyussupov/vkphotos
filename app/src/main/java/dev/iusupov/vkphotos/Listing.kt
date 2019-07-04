package dev.iusupov.vkphotos

import androidx.lifecycle.LiveData
import androidx.paging.PagedList

data class Listing<T>(
    val pagedList: LiveData<PagedList<T>>,
    val loadInitialNetworkState: LiveData<NetworkState>,
    val loadMoreNetworkState: LiveData<NetworkState>,
    val retry: LiveData<() -> Unit>)