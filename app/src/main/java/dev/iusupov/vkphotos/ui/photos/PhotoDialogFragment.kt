package dev.iusupov.vkphotos.ui.photos

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.State
import dev.iusupov.vkphotos.databinding.FragmentDialogPhotoBinding
import dev.iusupov.vkphotos.ext.getViewModel
import kotlinx.android.synthetic.main.toolbar.*

// TODO: implement zoom as in https://github.com/chrisbanes/PhotoView
class PhotoDialogFragment : DialogFragment() {

    private lateinit var viewModel: PhotosViewModel
    private lateinit var binding: FragmentDialogPhotoBinding
    private var isAppBarVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dialog_photo, container, false)

        val ownerId = arguments?.getInt(OWNER_ID_BUNDLE) ?: -1
        viewModel = activity?.getViewModel { PhotosViewModel(ownerId) } ?: return binding.root

        binding.lifecycleOwner = this
        binding.networkState = viewModel.openedPhotoState

        viewModel.openedPhoto.observe(this, Observer {
            if (it == null) {
                binding.photo.setImageResource(R.drawable.error_photo_placeholder)
            } else {
                binding.photo.setImageBitmap(it)
            }
        })

        handleErrorState()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAppBar()
    }

    private fun setupAppBar() {
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_close_white_24dp)
            setNavigationOnClickListener { dismiss() }
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }

        // TODO: show/hide app bar with animation and limited showing duration.
        binding.root.setOnClickListener {
            if (isAppBarVisible) {
                binding.appBar.visibility = View.GONE
                isAppBarVisible = false
            } else {
                binding.appBar.visibility = View.VISIBLE
                isAppBarVisible = true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            window?.setLayout(width, height)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.photo_dialog_menu, menu)
    }

    private fun handleErrorState() {
        viewModel.openedPhotoState.observe(this, Observer {
            if (it.state == State.ERROR) {
                val errorMsg = it.error?.message ?: getString(R.string.default_error_message)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    companion object {
        private const val OWNER_ID_BUNDLE = "owner_id"

        fun newInstance(ownerId: Int): DialogFragment {
            val fragment = PhotoDialogFragment()
            val args = Bundle()
            args.putInt(OWNER_ID_BUNDLE, ownerId)
            fragment.arguments = args

            return fragment
        }
    }
}