package dev.iusupov.vkphotos.model

data class Photo(val id: Int,
                 val albumId: Int,
                 val ownerId: Int,
                 val sizes: Map<String, Size>,
                 val text: String,
                 val date: Long,
                 val reposts: Int,
                 val likes: Int) {

    data class Size(val type: String,
                    val url: String,
                    val width: Int,
                    val height: Int)
}

