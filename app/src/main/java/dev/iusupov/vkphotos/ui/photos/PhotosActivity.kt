package dev.iusupov.vkphotos.ui.photos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.databinding.ActivityPhotosBinding
import dev.iusupov.vkphotos.ext.getViewModel
import dev.iusupov.vkphotos.model.PhotoItem
import dev.iusupov.vkphotos.utils.hasNetworkConnection
import dev.iusupov.vkphotos.vksdk.ERROR_CODE_NO_DATA
import dev.iusupov.vkphotos.vksdk.ERROR_CODE_PRIVATE_PROFILE
import kotlinx.android.synthetic.main.activity_photos.root
import kotlinx.android.synthetic.main.toolbar.*

class PhotosActivity : AppCompatActivity() {

    private lateinit var viewModel: PhotosViewModel
    private lateinit var binding: ActivityPhotosBinding
    private var ownerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_photos)

        ownerId = intent.getIntExtra(EXTRA_OWNER_ID, -1)
        viewModel = getViewModel { PhotosViewModel(ownerId) }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val title = intent.getStringExtra(EXTRA_FULL_NAME) ?: getString(R.string.app_name)
        setupAppBar(title)

        initAdapter()

        handleNetworkStates()

        observeFailed()
    }

    private fun setupAppBar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initAdapter() {
        val adapter = PhotosAdapter(this::onItemClick)
        binding.photosRv.adapter = adapter

        viewModel.photosListing.pagedList.observe(this, Observer { photoItems ->
            adapter.submitList(photoItems)
        })

        viewModel.photosListing.loadMoreNetworkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun handleNetworkStates() {
        viewModel.photosListing.loadInitialNetworkState.observe(this, Observer { networkState ->
            when (networkState.state) {
                State.ERROR -> {
                    when {
                        networkState.error?.code == ERROR_CODE_PRIVATE_PROFILE -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.profile_is_private)
                        }
                        networkState.error?.code == ERROR_CODE_NO_DATA -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.photos_empty_state)
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

        viewModel.photosListing.loadMoreNetworkState.observe(this, Observer { networkState ->
            if (networkState.state == State.ERROR) {
                if (hasNetworkConnection(this)) {
                    retryPopUp(getString(R.string.default_try_again_message))
                } else {
                    retryPopUp(getString(R.string.check_connection_and_retry))
                }
            }
        })
    }

    private fun observeFailed() {
        viewModel.photosListing.retry.observe(this, Observer {
            viewModel.retry = it
        })
    }

    private fun retryPopUp(message: String) {
        Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry)) {
            viewModel.retry?.invoke()
        }.show()
    }

    private fun onItemClick(photoItem: PhotoItem) {
        viewModel.loadOpenedPhoto(photoItem)
        openPhotoDialogFragment(ownerId)
    }

    private fun openPhotoDialogFragment(ownerId: Int) {
        val fragment = PhotoDialogFragment.newInstance(ownerId)
        val transaction = supportFragmentManager.beginTransaction().apply {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        }
        fragment.show(transaction, null)
    }

    companion object {
        const val EXTRA_OWNER_ID = "owner_id"
        const val EXTRA_FULL_NAME = "full_name"
    }
}
