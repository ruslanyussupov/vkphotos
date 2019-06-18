package dev.iusupov.vkphotos.repository

import com.vk.api.sdk.VK
import dev.iusupov.vkphotos.FriendsRequest
import dev.iusupov.vkphotos.model.FriendsResponse

class ApiImpl : Api {

    override fun fetchFriends(userId: Int, count: Int, offset: Int): FriendsResponse {
        val request = FriendsRequest(count, offset, userId)
        return VK.executeSync(request)
    }
}