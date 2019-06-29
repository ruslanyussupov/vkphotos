package dev.iusupov.vkphotos

import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.PhotosResponse
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.Api

class FakeApi(private val count: Int = 100,
              var error: Exception? = null,
              var withRecovery: Boolean = false) : Api {

    private var fetchingFriendsThrownErrors = 0

    val users by lazy {
        List(count) { index ->
            User(index, "Jane_$index", "Doe_$index",
                "https://sun1-16.userapi.com/c636327/v636327034/2be84/TYzZpZ8BL0k.jpg")
        }
    }

    override fun fetchFriends(userId: Int, count: Int, offset: Int): FriendsResponse {
        error?.let { error ->
            if (withRecovery) {
                if (fetchingFriendsThrownErrors < 2) {
                    fetchingFriendsThrownErrors++
                    throw error
                } else {
                    fetchingFriendsThrownErrors = 0
                }
            } else {
                throw error
            }
        }
        val result = users.subList(offset, offset + count)
        return FriendsResponse(count, result)
    }

    override fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse {
        TODO("not implemented")
    }
}