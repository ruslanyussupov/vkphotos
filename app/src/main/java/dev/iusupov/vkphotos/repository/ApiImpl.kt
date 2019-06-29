package dev.iusupov.vkphotos.repository

import com.vk.api.sdk.VK
import dev.iusupov.vkphotos.FriendsRequest
import dev.iusupov.vkphotos.PhotosRequest
import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.PhotosResponse

class ApiImpl : Api {

    override fun fetchFriends(userId: Int, count: Int, offset: Int): FriendsResponse {
        val request = FriendsRequest(count, offset, userId)
        return VK.executeSync(request)
    }

    override fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse {
        val request = PhotosRequest(ownerId, count, offset)
        return VK.executeSync(request)
    }
}