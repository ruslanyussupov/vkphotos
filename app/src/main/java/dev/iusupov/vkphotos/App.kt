package dev.iusupov.vkphotos

import android.app.Application
import android.content.Intent
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import dev.iusupov.vkphotos.ui.MainActivity
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : Application() {

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            Timber.i("VK token expired.")
            launchMainActivity()
        }
    }

    override fun onCreate() {
        super.onCreate()

        setupTimber()
        setupVkSdk()
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    private fun setupVkSdk() {
        VK.addTokenExpiredHandler(tokenTracker)
        VK.initialize(applicationContext)
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}