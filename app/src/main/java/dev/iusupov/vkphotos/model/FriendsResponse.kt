package dev.iusupov.vkphotos.model

data class FriendsResponse(val count: Int,
                           val users: List<User>)