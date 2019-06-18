package dev.iusupov.vkphotos.ui.friends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.State
import dev.iusupov.vkphotos.hasNetworkConnection
import dev.iusupov.vkphotos.ui.MainActivity
import kotlinx.android.synthetic.main.activity_friends.*


class FriendsActivity : AppCompatActivity() {

    private lateinit var viewModel: FriendsViewModel
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        viewModel = ViewModelProviders.of(this).get(FriendsViewModel::class.java)

        initAdapter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == R.id.action_log_out) {
            VK.logout()
            launchMainActivity()
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun initAdapter() {
        adapter = UserAdapter(viewModel.viewModelScope)
        friends_rv.adapter = adapter

        viewModel.friendsListing.pagedList.observe(this, Observer { users ->
            adapter.submitList(users)
        })

        viewModel.friendsListing.networkState.observe(this, Observer {networkState ->
            if (networkState.state == State.ERROR) {
                if (hasNetworkConnection(this)) {
                    retryPopUp(networkState.errorMsg)
                } else {
                    retryPopUp(getString(R.string.no_network_connection))
                }
            }
        })
    }

    private fun retryPopUp(message: String) {
        Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry)) {
            viewModel.friendsListing.retry()
        }.show()
    }

    private fun launchMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
