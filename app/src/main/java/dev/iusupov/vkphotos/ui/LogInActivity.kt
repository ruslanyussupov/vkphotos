package dev.iusupov.vkphotos.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import dev.iusupov.vkphotos.ui.friends.FriendsActivity
import dev.iusupov.vkphotos.R
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        setupActionBar()

        setLogInBtnOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        checkIsLoggedIn()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!isLoginResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun logIn() {
        Timber.i("Logging in...")
        VK.login(this, arrayListOf(VKScope.FRIENDS, VKScope.PHOTOS))
    }

    private fun checkIsLoggedIn() {
        if (VK.isLoggedIn()) {
            Timber.i("Logged in.")
            launchFriendsActivity()
            finish()
        } else {
            log_in_btn.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun isLoginResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (data == null) return false

        return VK.onActivityResult(requestCode, resultCode, data, object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                Timber.i("Authorization process completed. ${token.accessToken}")
                saveToken(token.accessToken)
                launchFriendsActivity()
                finish()
            }

            override fun onLoginFailed(errorCode: Int) {
                // TODO: handle cancellation
                Timber.e("Something went wrong while the authorization. Error code: $errorCode")
                showAuthFailed()
            }
        })
    }

    private fun showAuthFailed() {
        Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
    }

    private fun saveToken(accessToken: String) {
        val sharedPref = getSharedPreferences(getString(R.string.vk_sdk_prefs), Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(getString(R.string.access_token), accessToken)
        }
    }

    private fun setLogInBtnOnClickListener() {
        log_in_btn.setOnClickListener {
            it.visibility = View.GONE
            logIn()
        }
    }

    private fun launchFriendsActivity() {
        val intent = Intent(this, FriendsActivity::class.java)
        startActivity(intent)
    }
}
