package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.PhotosResponse

interface Api {

    suspend fun fetchFriends(userId: Int = -1, count: Int, offset: Int): FriendsResponse

    suspend fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse
}