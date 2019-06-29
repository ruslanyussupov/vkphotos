package dev.iusupov.vkphotos

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(val state: State,
                                            val error: Error? = null) {
    companion object {
        val LOADING = NetworkState(State.RUNNING)
        val LOADED = NetworkState(State.SUCCESS)
        fun error(message: String, code: Int = -1) = NetworkState(State.ERROR, Error(message, code))
    }
}

enum class State {
    RUNNING,
    SUCCESS,
    ERROR
}

data class Error(val message: String,
                 val code: Int)