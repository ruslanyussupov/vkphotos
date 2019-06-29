package dev.iusupov.vkphotos.ui.friends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.databinding.ActivityFriendsBinding
import dev.iusupov.vkphotos.ext.getViewModel
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.ui.MainActivity
import dev.iusupov.vkphotos.ui.photos.PhotosActivity
import kotlinx.android.synthetic.main.activity_friends.*
import kotlinx.android.synthetic.main.toolbar.*


class FriendsActivity : AppCompatActivity() {

    private val viewModel by lazy {
        getViewModel<FriendsViewModel>()
    }
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityFriendsBinding = DataBindingUtil.setContentView(this, R.layout.activity_friends)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setUpActionBar()

        initAdapter()

        handleNetworkStates()
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

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun initAdapter() {
        adapter = UserAdapter(viewModel.viewModelScope, this::onUserClick)
        friends_rv.adapter = adapter

        viewModel.friendsListing.pagedList.observe(this, Observer { users ->
            adapter.submitList(users)
        })
    }

    private fun handleNetworkStates() {
        viewModel.friendsListing.loadInitialNetworkState.observe(this, Observer { networkState ->
            when (networkState.state) {
                State.ERROR -> {
                    when {
                        networkState.error?.code == ERROR_CODE_PRIVATE_PROFILE -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.profile_is_private)
                        }
                        networkState.error?.code == ERROR_CODE_NO_DATA -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.friends_empty_state)
                        }
                        hasNetworkConnection(this) -> {
                            val errorMsg = networkState.error?.message ?: getString(R.string.default_error_message)
                            viewModel.stateText.value = errorMsg
                            viewModel.isLoading.set(false)
                            retryPopUp(getString(R.string.default_try_again_message))
                        }
                        else -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.no_network_connection)
                            retryPopUp(getString(R.string.check_connection_and_retry))
                        }
                    }
                }
                State.SUCCESS -> {
                    viewModel.isLoading.set(false)
                    viewModel.stateText.value = null
                }
                else -> {
                    viewModel.isLoading.set(true)
                    viewModel.stateText.value = null
                }
            }
        })

        viewModel.friendsListing.loadMoreNetworkState.observe(this, Observer { networkState ->
            if (networkState.state == State.ERROR) {
                if (hasNetworkConnection(this)) {
                    retryPopUp(getString(R.string.default_try_again_message))
                } else {
                    retryPopUp(getString(R.string.check_connection_and_retry))
                }
            }
        })
    }

    private fun onUserClick(user: User) {
        val fullName = "${user.firstName} ${user.lastName}"
        launchPhotosActivity(user.id, fullName)
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

    private fun launchPhotosActivity(ownerId: Int, fullName: String) {
        val intent = Intent(this, PhotosActivity::class.java)
        intent.putExtra(PhotosActivity.EXTRA_OWNER_ID, ownerId)
        intent.putExtra(PhotosActivity.EXTRA_FULL_NAME, fullName)
        startActivity(intent)
    }
}
