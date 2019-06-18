package dev.iusupov.vkphotos

import androidx.lifecycle.Observer

class LoggingObserver<T> : Observer<T> {

    var value: T? = null

    override fun onChanged(t: T) {
        value = t
    }
}