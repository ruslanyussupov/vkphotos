package dev.iusupov.vkphotos.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.loadBitmapAsync
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.parseUrlFromString
import dev.iusupov.vkphotos.ext.toRoundedDrawable
import kotlinx.android.synthetic.main.item_user.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class UserAdapter(private val scope: CoroutineScope,
                  private val onItemClick: ((user: User) -> Unit)? = null) : PagedListAdapter<User, UserAdapter.UserViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, scope)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position) ?: User(0, "", "", "")
        holder.bind(user)
        holder.itemView.setOnClickListener {
            Timber.d("On item clicked: ${user.firstName} ${user.lastName}")
            onItemClick?.invoke(user)
        }
    }

    class UserViewHolder(view: View, private val scope: CoroutineScope) : RecyclerView.ViewHolder(view) {

        private var job: Job? = null

        fun bind(user: User) {
            itemView.photo.setImageResource(R.drawable.camera_circle_100)

            if (user.firstName.isEmpty() && user.lastName.isEmpty()) {
                showPlaceholders()
            } else {
                showUserData(user)
            }

            loadPhotoIntoImageView(user.photo, itemView.photo)
        }

        private fun showPlaceholders() {
            itemView.full_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.grey_10))
                text = resources.getString(R.string.empty_name_placeholder, "")
            }
        }

        private fun showUserData(user: User) {
            itemView.full_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                text = resources.getString(R.string.full_name, user.firstName, user.lastName)
            }
        }

        private fun loadPhotoIntoImageView(photoUrl: String, view: ImageView) {
            job?.cancel()

            parseUrlFromString(photoUrl)?.let { url ->
                job = scope.launch {
                    try {
                        val deferred = scope.loadBitmapAsync(url)
                        val bitmap = deferred.await()
                        withContext(Dispatchers.Main) {
                            view.setImageDrawable(bitmap?.toRoundedDrawable(itemView.resources))
                        }
                        return@launch

                    } catch (error: Exception) {
                        Timber.e("Loading user's photo is failed: url=$photoUrl. $error")
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_UTIL = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}