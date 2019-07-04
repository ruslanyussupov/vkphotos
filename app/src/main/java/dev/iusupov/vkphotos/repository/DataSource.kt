package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.Listing
import dev.iusupov.vkphotos.model.PhotoItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

interface DataSource {

    fun fetchFriends(coroutineScope: CoroutineScope,
                     userId: Int = -1,
                     pageSize: Int = 20,
                     networkDispatcher: CoroutineDispatcher = Dispatchers.IO): Listing<User>

    fun fetchPhotos(ownerId: Int,
                    coroutineScope: CoroutineScope,
                    pageSize: Int = 20,
                    networkDispatcher: CoroutineDispatcher = Dispatchers.IO): Listing<PhotoItem>
}