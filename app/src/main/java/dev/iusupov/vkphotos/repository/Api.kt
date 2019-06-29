package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.PhotosResponse

interface Api {

    fun fetchFriends(userId: Int = -1, count: Int, offset: Int): FriendsResponse

    fun fetchPhotos(ownerId: Int, count: Int, offset: Int): PhotosResponse
}