package dev.iusupov.vkphotos.ui.photos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import dev.iusupov.vkphotos.Error
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.databinding.FragmentDialogPhotoBinding
import dev.iusupov.vkphotos.ext.getViewModel
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class PhotoDialogFragment : DialogFragment() {

    private lateinit var viewModel: PhotosViewModel
    private lateinit var binding: FragmentDialogPhotoBinding
    private var shortAnimationDuration = 0
    private val handler = Handler()

    private val hideSystemUiRunnable = Runnable {
        hideStatusBar()
    }

    private val onSystemUiVisibilityChangeListener: (Int) -> Unit = { visibility ->
        if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
            showToolbar()
            handler.postDelayed(hideSystemUiRunnable, 3_000L)
        } else {
            hideToolbar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dialog_photo, container, false)

        binding.photo.setOnViewTapListener { _, _, _ ->
            if (isSystemUiVisible()) {
                handler.removeCallbacks(hideSystemUiRunnable)
                hideSystemUiRunnable.run()
            } else {
                showStatusBar()
            }
        }

        val ownerId = arguments!!.getInt(OWNER_ID_BUNDLE)
        viewModel = activity!!.getViewModel { PhotosViewModel(ownerId) }

        binding.lifecycleOwner = this
        binding.networkState = viewModel.openedPhotoState

        viewModel.openedPhoto.observe(viewLifecycleOwner, Observer {
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
        setupToolbar()
    }

    private fun setupToolbar() {
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_close_white_24dp)
            setNavigationOnClickListener { dismiss() }
            setBackgroundColor(ContextCompat.getColor(context, R.color.transparent_black))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window!!.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            setLayout(width, height)
            decorView.setOnSystemUiVisibilityChangeListener(onSystemUiVisibilityChangeListener)
        }
    }

    override fun onResume() {
        super.onResume()
        showStatusBar()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(hideSystemUiRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("#onDestroy")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.photo_dialog_menu, menu)
    }

    private fun handleErrorState() {
        viewModel.openedPhotoState.observe(this, Observer { networkState ->
            if (networkState is Error) {
                val errorMsg = networkState.message
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showToolbar() {
        toolbar.apply {
            alpha = 0.0f
            visibility = View.VISIBLE
            animate()
                .alpha(1.0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    private fun hideToolbar() {
        toolbar.animate()
            .alpha(0.0f)
            .setDuration(shortAnimationDuration.toLong())
            .setListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator?) {
                    toolbar.visibility = View.INVISIBLE
                }
            })
    }

    private fun showStatusBar() {
        dialog!!.window!!.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    private fun hideStatusBar() {
        dialog!!.window!!.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun isSystemUiVisible(): Boolean {
        return toolbar.isVisible
                && (dialog!!.window!!.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0)
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