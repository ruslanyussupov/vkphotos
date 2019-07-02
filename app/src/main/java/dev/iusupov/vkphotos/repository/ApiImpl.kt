package dev.iusupov.vkphotos.repository

import com.vk.api.sdk.VK
import dev.iusupov.vkphotos.vksdk.FriendsRequest
import dev.iusupov.vkphotos.vksdk.PhotosRequest
import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.PhotosResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiImpl : Api {

    override suspend fun fetchFriends(userId: Int, count: Int, offset: Int): FriendsResponse {
        return withContext(Dispatchers.IO) {
            val request = FriendsRequest(count, offset, userId)
            VK.executeSync(request)
        }
    }

    override suspend fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse {
        return withContext(Dispatchers.IO) {
            val request = PhotosRequest(ownerId, count, offset)
            VK.executeSync(request)
        }
    }
}