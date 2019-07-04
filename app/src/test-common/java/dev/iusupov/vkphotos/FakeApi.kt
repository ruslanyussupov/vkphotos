package dev.iusupov.vkphotos

import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.PhotosResponse
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.repository.Api

class FakeApi(private val count: Int = 100,
              var error: Exception? = null,
              var withRecovery: Boolean = false) : Api {

    private var fetchingFriendsThrownErrors = 0
    private var fetchingPhotosThrownErrors = 0

    val users by lazy {
        List(count) { index ->
            User(index, "Jane_$index", "Doe_$index",
                "https://sun1-21.userapi.com/g6ZGmhbzsB5gLf2kSAmjq1_9F38ndAoDMF2sDQ/kAyBWVQHbpg.jpg")
        }
    }

    val photos by lazy {
        List(count) { index ->
            val sizes = mutableMapOf<String, Photo.Size>()
/*            val sizeQ =
                Photo.Size(
                    type = "q",
                    url = "https://sun1-26.userapi.com/c846021/v846021265/4645f/bSs-QvBWano.jpg",
                    width = 320,
                    height = 448)
            val sizeX =
                Photo.Size(
                    type = "x",
                    url = "https://sun1-18.userapi.com/c846021/v846021265/46459/FMsSfS06iEA.jpg",
                    width = 431,
                    height = 604)
            sizes.apply {
                put("q", sizeQ)
                put("x", sizeX)
            }*/

            Photo(
                id = index,
                albumId = index,
                ownerId = 1,
                sizes = sizes,
                text = "",
                date = System.currentTimeMillis(),
                reposts = index,
                likes = index)
        }
    }

    override suspend fun fetchFriends(userId: Int, count: Int, offset: Int): FriendsResponse {
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
        return FriendsResponse(users.size, result)
    }

    override suspend fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse {
        error?.let { error ->
            if (withRecovery) {
                if (fetchingPhotosThrownErrors < 2) {
                    fetchingPhotosThrownErrors++
                    throw error
                } else {
                    fetchingFriendsThrownErrors = 0
                }
            } else {
                throw error
            }
        }
        val result = photos.subList(offset, offset + count)
        return PhotosResponse(photos.size, result)
    }
}