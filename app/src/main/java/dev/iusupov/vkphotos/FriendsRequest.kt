package dev.iusupov.vkphotos

import com.vk.api.sdk.requests.VKRequest
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
        val count = r.getJSONObject(JSON_RESPONSE).getInt(JSON_COUNT)
        val usersJson = r.getJSONObject(JSON_RESPONSE).getJSONArray(JSON_ITEMS)
        val users = ArrayList<User>(usersJson.length())

        for (i in 0 until usersJson.length()) {
            val user = usersJson.getJSONObject(i).parseToUser()
            users.add(user)
        }

        return FriendsResponse(count, users)
    }
}
