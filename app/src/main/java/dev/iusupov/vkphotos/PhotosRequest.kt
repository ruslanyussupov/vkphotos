package dev.iusupov.vkphotos

import com.vk.api.sdk.requests.VKRequest
import dev.iusupov.vkphotos.ext.parseToPhoto
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.model.PhotosResponse
import org.json.JSONObject

class PhotosRequest(ownerId: Int, count: Int, offset: Int) : VKRequest<PhotosResponse>("photos.getAll") {

    init {
        addParam(PARAM_OWNER_ID, ownerId)
        addParam(PARAM_COUNT, count)
        addParam(PARAM_OFFSET, offset)
        addParam(PARAM_EXTENDED, 1)
        addParam(PARAM_PHOTO_SIZES, 1)
        addParam(PARAM_SKIP_HIDDEN, 0)
        addParam(PARAM_NEED_HIDDEN, 0)
        addParam(PARAM_NO_SERVICE_ALBUMS, 1)
    }

    override fun parse(r: JSONObject): PhotosResponse {
        val response = r.getJSONObject(JSON_RESPONSE)
        val count = response.optInt(JSON_COUNT)
        val photos = response.getJSONArray(JSON_ITEMS).let {
            val result = ArrayList<Photo>(it.length())

            for (i in 0..it.length()) {
                result += it.optJSONObject(i)?.parseToPhoto() ?: continue
            }

            result
        }

        return PhotosResponse(count, photos)
    }
}