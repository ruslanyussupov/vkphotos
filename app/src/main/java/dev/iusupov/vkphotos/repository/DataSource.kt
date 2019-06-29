package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.Listing
import dev.iusupov.vkphotos.model.PhotoItem
import kotlinx.coroutines.CoroutineScope

interface DataSource {

    fun fetchFriends(userId: Int = -1, pageSize: Int = 20, coroutineScope: CoroutineScope): Listing<User>

    fun fetchPhotos(ownerId: Int, pageSize: Int = 20, coroutineScope: CoroutineScope): Listing<PhotoItem>
}