package dev.iusupov.vkphotos.model

data class PhotosResponse(val count: Int,
                          val photos: List<Photo>)