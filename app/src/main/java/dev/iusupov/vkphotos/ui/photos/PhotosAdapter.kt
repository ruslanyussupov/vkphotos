package dev.iusupov.vkphotos.ui.photos

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.iusupov.vkphotos.Loading
import dev.iusupov.vkphotos.NetworkState
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.model.Photo
import dev.iusupov.vkphotos.utils.NetworkUtils
import kotlinx.android.synthetic.main.item_photo.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class PhotosAdapter(private val coroutineScope: CoroutineScope,
                    private val networkUtils: NetworkUtils,
                    private var onItemClick: ((photo: Photo, thumbnail: Bitmap?) -> Unit)?)
    : PagedListAdapter<Photo, RecyclerView.ViewHolder>(DIFF_UTIL) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
            PhotoViewHolder(view, coroutineScope, networkUtils)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_progress_bar, parent, false)
            ProgressBarViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams

        if (holder is PhotoViewHolder) {
            val photo = getItem(position) ?: return
            layoutParams.isFullSpan = false
            holder.bind(photo)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(photo, holder.thumbnail)
            }
        } else {
            layoutParams.isFullSpan = true
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            VIEW_PROGRESS
        } else {
            VIEW_ITEM
        }
    }

    private fun hasExtraRow() = networkState != null && networkState is Loading

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    class PhotoViewHolder(view: View,
                          private val coroutineScope: CoroutineScope,
                          private val networkUtils: NetworkUtils) : RecyclerView.ViewHolder(view) {

        private var job: Job? = null
        var thumbnail: Bitmap? = null

        fun bind(photo: Photo) {
            itemView.photo.setImageBitmap(null)
            job?.cancel()
            thumbnail = null

            job = coroutineScope.launch {
                val url = photo.run {
                    sizes["q"]?.url ?: sizes["x"]?.url ?: sizes["m"]?.url
                }
                if (url == null) {
                    withContext(Dispatchers.Main) {
                        itemView.photo.setImageResource(R.drawable.error_photo_placeholder)
                    }
                } else {
                    val bitmap = networkUtils.loadBitmapWithCaching(url)
                    thumbnail = bitmap
                    withContext(Dispatchers.Main) {
                        itemView.photo.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    class ProgressBarViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val VIEW_ITEM = 0
        private const val VIEW_PROGRESS = 1

        private val DIFF_UTIL = object : DiffUtil.ItemCallback<Photo>() {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }
        }
    }
}