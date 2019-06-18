package dev.iusupov.vkphotos.repository

import dev.iusupov.vkphotos.model.FriendsResponse

interface Api {

    fun fetchFriends(userId: Int = -1, count: Int, offset: Int): FriendsResponse

}