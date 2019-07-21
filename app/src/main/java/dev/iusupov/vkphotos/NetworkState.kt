package dev.iusupov.vkphotos

sealed class NetworkState
object Loading : NetworkState()
object Loaded : NetworkState()
data class Error(val message: String,
                 val code: Int = -1) : NetworkState() {

    companion object {
        const val ERROR_CODE_PRIVATE_PROFILE = 30
        const val ERROR_CODE_NO_DATA = 404
    }
}