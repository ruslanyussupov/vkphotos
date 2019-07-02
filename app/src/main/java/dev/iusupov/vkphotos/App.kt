package dev.iusupov.vkphotos

import android.app.Application
import android.content.Intent
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler
import dev.iusupov.vkphotos.di.ApplicationComponent
import dev.iusupov.vkphotos.di.DaggerApplicationComponent
import dev.iusupov.vkphotos.ui.LogInActivity
import timber.log.Timber
import timber.log.Timber.DebugTree

class App : Application() {

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            Timber.i("VK token expired.")
            launchLogInActivity()
        }
    }

    override fun onCreate() {
        super.onCreate()

        setupDaggerGraph()
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

    private fun setupDaggerGraph() {
        appComponent = DaggerApplicationComponent.builder()
            .applicationContext(this)
            .build()
    }

    private fun launchLogInActivity() {
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    companion object {
        var appComponent: ApplicationComponent? = null
        val dataComponent by lazy {
            appComponent!!.dataComponentBuilder().build()
        }
    }
}