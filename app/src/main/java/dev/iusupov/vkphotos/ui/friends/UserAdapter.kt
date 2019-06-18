package dev.iusupov.vkphotos.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.iusupov.vkphotos.R
import dev.iusupov.vkphotos.loadBitmap
import dev.iusupov.vkphotos.model.User
import dev.iusupov.vkphotos.parseUrlFromString
import kotlinx.android.synthetic.main.item_user.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

class UserAdapter(private val scope: CoroutineScope) : PagedListAdapter<User, UserAdapter.UserViewHolder>(DIFF_UTIL) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view, scope)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position) ?: User(0, "", "", "")
        holder.bind(user)
    }

    class UserViewHolder(view: View, private val scope: CoroutineScope) : RecyclerView.ViewHolder(view) {

        private var job: Job? = null

        fun bind(user: User) {
            job?.cancel()

            itemView.photo.setImageResource(R.drawable.camera_100)

            if (user.firstName.isEmpty() && user.lastName.isEmpty()) {
                showPlaceholders()
            } else {
                showUserData(user)
            }

            loadPhoto(user.photo)
        }

        private fun showPlaceholders() {
            itemView.first_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.textViewPlaceholder))
                text = "                   "
            }
            itemView.last_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.textViewPlaceholder))
                text = "                   "
            }
        }

        private fun showUserData(user: User) {
            itemView.first_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                text = user.firstName
            }
            itemView.last_name.apply {
                setBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.transparent))
                text = user.lastName
            }
        }

        private fun loadPhoto(photoUrl: String) {
            parseUrlFromString(photoUrl)?.let { url ->
                job = scope.launch {
                    repeat(3) {
                        try {
                            val deferred = scope.loadBitmap(url)
                            val bitmap = deferred.await()
                            withContext(Dispatchers.Main) {
                                itemView.photo.setImageBitmap(bitmap)
                            }
                            return@launch

                        } catch (error: IOException) {
                            Timber.e("Loading user's photo is failed. URL=$photoUrl $error")
                        } catch (error: SocketTimeoutException) {
                            Timber.e(error)
                        }
                    }
                }
            }
        }
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}