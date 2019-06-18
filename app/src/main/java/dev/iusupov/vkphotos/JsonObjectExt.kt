package dev.iusupov.vkphotos

import dev.iusupov.vkphotos.model.User
import org.json.JSONObject

fun JSONObject.parseToUser(): User {
    val id = optInt(JSON_ID)
    val firstName = optString(JSON_FIRST_NAME)
    val lastName = optString(JSON_LAST_NAME)
    val photo = optString(JSON_PHOTO_100)
    return User(id, firstName, lastName, photo)
}