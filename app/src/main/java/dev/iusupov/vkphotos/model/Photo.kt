package dev.iusupov.vkphotos.model

data class Photo(val id: Int,
                 val albumId: Int,
                 val ownerId: Int,
                 val sizes: ArrayList<Size>) {

    data class Size(val type: Char,
                    val url: String,
                    val width: Int,
                    val height: Int)

}

