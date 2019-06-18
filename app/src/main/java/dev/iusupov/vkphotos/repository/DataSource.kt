package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.Listing
import kotlinx.coroutines.CoroutineScope

interface DataSource {

    fun fetchFriends(userId: Int = -1, pageSize: Int = 20, coroutineScope: CoroutineScope): Listing<User>

    fun getPhotos(ownerId: Int)

}