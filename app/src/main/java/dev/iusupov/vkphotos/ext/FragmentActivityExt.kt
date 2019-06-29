package dev.iusupov.vkphotos.ext

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dev.iusupov.vkphotos.ViewModelFactory

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(noinline creator: (() -> T)? = null): T {
    return if (creator == null) {
        ViewModelProviders.of(this).get(T::class.java)
    } else {
        ViewModelProviders.of(this, ViewModelFactory(creator)).get(T::class.java)
    }
}