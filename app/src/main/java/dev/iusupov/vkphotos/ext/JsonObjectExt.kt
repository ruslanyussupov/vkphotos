package dev.iusupov.vkphotos.ext

import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.vksdk.*
import org.json.JSONObject

fun JSONObject.parseToUser(): User {
    val id = optInt(JSON_ID)
    val firstName = optString(JSON_FIRST_NAME)
    val lastName = optString(JSON_LAST_NAME)
    val photo = optString(JSON_PHOTO_100)
    return User(id, firstName, lastName, photo)
}

fun JSONObject.parseToPhoto(): Photo {
    val id = optInt(JSON_ID)
    val ownerId = optInt(JSON_OWNER_ID)
    val albumId = optInt(JSON_ALBUM_ID)
    val text = optString(JSON_TEXT)
    val date = optLong(JSON_DATE)
    val likes = optJSONObject(JSON_LIKES)?.optInt(JSON_COUNT) ?: 0
    val reposts = optJSONObject(JSON_REPOSTS)?.optInt(JSON_COUNT) ?: 0
    val sizes = optJSONArray(JSON_SIZES)?.let {
        val result = HashMap<String, Photo.Size>(it.length())
        for (i in 0..it.length()) {
            val size = it.optJSONObject(i)?.parseToSize() ?: continue
            result += size.type to size
        }
        result
    } ?: emptyMap<String, Photo.Size>()
    return Photo(id, albumId, ownerId, sizes, text, date, reposts, likes)
}

fun JSONObject.parseToSize(): Photo.Size {
    val type = optString(JSON_TYPE)
    val url = optString(JSON_URL)
    val width = optInt(JSON_WIDTH)
    val height = optInt(JSON_HEIGHT)
    return Photo.Size(type, url, width, height)
}