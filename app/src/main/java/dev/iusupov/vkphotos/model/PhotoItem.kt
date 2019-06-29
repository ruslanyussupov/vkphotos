package dev.iusupov.vkphotos.model

import android.graphics.Bitmap

data class PhotoItem(val id: Int,
                     val thumbnail: Bitmap?,
                     val originalUrl: String?,
                     val text: String,
                     val date: Long,
                     val reposts: Int,
                     val likes: Int)