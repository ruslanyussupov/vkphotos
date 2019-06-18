package dev.iusupov.vkphotos

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(val state: State,
                                            val errorMsg: String = "") {
    companion object {
        val LOADING = NetworkState(State.RUNNING)
        val LOADED = NetworkState(State.SUCCESS)
        fun error(errorMsg: String) = NetworkState(State.ERROR, errorMsg)
    }
}

enum class State {
    RUNNING,
    SUCCESS,
    ERROR
}