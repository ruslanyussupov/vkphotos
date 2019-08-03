package dev.iusupov.vkphotos.ui.photos

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import dev.iusupov.vkphotos.*
import dev.iusupov.vkphotos.databinding.ActivityPhotosBinding
import dev.iusupov.vkphotos.ext.getViewModel
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.utils.hasNetworkConnection
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
    }

    private fun setupAppBar(title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            this.title = title
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initAdapter() {
        val adapter = PhotosAdapter(viewModel.viewModelScope, viewModel.networkUtils, this::onItemClick)
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
            when (networkState) {
                is Error -> {
                    when {
                        networkState.code == Error.ERROR_CODE_PRIVATE_PROFILE -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.profile_is_private)
                        }
                        networkState.code == Error.ERROR_CODE_NO_DATA -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.photos_empty_state)
                        }
                        !hasNetworkConnection(this) -> {
                            viewModel.isLoading.set(false)
                            viewModel.stateText.value = getString(R.string.no_network_connection)
                            retryPopUp(getString(R.string.check_connection_and_retry))
                        }
                        else -> {
                            val errorMsg = networkState.message
                            viewModel.stateText.value = errorMsg
                            viewModel.isLoading.set(false)
                            retryPopUp(getString(R.string.default_try_again_message))
                        }
                    }
                }
                Loaded -> {
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
            if (networkState is Error) {
                if (!hasNetworkConnection(this)) {
                    retryPopUp(getString(R.string.check_connection_and_retry))
                } else {
                    retryPopUp(getString(R.string.default_try_again_message))
                }
            }
        })
    }

    private fun retryPopUp(message: String) {
        Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.retry)) {
            viewModel.retry()
        }.show()
    }

    private fun onItemClick(photo: Photo, thumbnail: Bitmap?) {
        viewModel.loadOpenedPhoto(photo, thumbnail)
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
