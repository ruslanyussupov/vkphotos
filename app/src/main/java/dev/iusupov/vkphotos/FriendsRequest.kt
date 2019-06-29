package dev.iusupov.vkphotos

import com.vk.api.sdk.requests.VKRequest
import dev.iusupov.vkphotos.ext.parseToUser
import dev.iusupov.vkphotos.model.FriendsResponse
import dev.iusupov.vkphotos.model.User
import org.json.JSONObject

class FriendsRequest(count: Int, offset: Int, userId: Int = -1) : VKRequest<FriendsResponse>("friends.get") {

    init {
        if (userId > 0) {
            addParam(PARAM_USER_ID, userId)
        }
        addParam(PARAM_ORDER, VAL_HINTS)
        addParam(PARAM_FIELDS, VAL_PHOTO_100)
        addParam(PARAM_COUNT, count)
        addParam(PARAM_OFFSET, offset)
    }

    override fun parse(r: JSONObject): FriendsResponse {
        val response = r.getJSONObject(JSON_RESPONSE)
        val count = response.optInt(JSON_COUNT)
        val users = response.getJSONArray(JSON_ITEMS).let {
            val result = ArrayList<User>(it.length())

            for (i in 0 until it.length()) {
                result += it.optJSONObject(i)?.parseToUser() ?: continue
            }

            result
        }

        return FriendsResponse(count, users)
    }
}
