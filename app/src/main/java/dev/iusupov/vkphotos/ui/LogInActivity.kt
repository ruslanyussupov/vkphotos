package dev.iusupov.vkphotos.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import dev.iusupov.vkphotos.ui.friends.FriendsActivity
import dev.iusupov.vkphotos.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpActionBar()

        login()

        setTryAgainBtnOnClickListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!isLoginResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun login() {
        if (!VK.isLoggedIn()) {
            Timber.i("Logging in...")
            VK.login(this, arrayListOf(VKScope.FRIENDS, VKScope.PHOTOS))
        } else {
            Timber.i("Logged in.")
            launchFriendsActivity()
            finish()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun isLoginResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (data == null) return false

        return VK.onActivityResult(requestCode, resultCode, data, object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                Timber.i("Authorization process completed. ${token.accessToken}")
                saveToken(token.accessToken)
                hideErrorState()
                launchFriendsActivity()
                finish()
            }

            override fun onLoginFailed(errorCode: Int) {
                Timber.e("Something went wrong while the authorization. Error code: $errorCode")
                showErrorState()
            }
        })
    }

    private fun saveToken(accessToken: String) {
        val sharedPref = getSharedPreferences(getString(R.string.vk_sdk_prefs), Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(getString(R.string.access_token), accessToken)
        }
    }

    private fun showErrorState() {
        auth_status.visibility = View.VISIBLE
        btn_try_again.visibility = View.VISIBLE
    }

    private fun hideErrorState() {
        auth_status.visibility = View.GONE
        btn_try_again.visibility = View.GONE
    }

    private fun setTryAgainBtnOnClickListener() {
        btn_try_again.setOnClickListener {
            login()
        }
    }

    private fun launchFriendsActivity() {
        val intent = Intent(this, FriendsActivity::class.java)
        startActivity(intent)
    }
}
