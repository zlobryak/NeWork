package ru.netology.nework.ui.adapters.eventFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.databinding.EventCardBinding
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import ru.netology.nework.view.loadAvatar

class EventsPagingAdapter(
    private val currentUserId: Int,
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<EventItem, EventsPagingAdapter.EventViewHolder>(EventItemDiffCallback()) {

    interface OnInteractionListener {
        fun onLike(event: EventItem) {}
        fun onEdit(event: EventItem) {}
        fun onRemove(event: EventItem) {}
        fun onShare(event: EventItem) {}
        fun onAuthorClick(userId: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = EventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let { event ->
            holder.bind(event)
        }
    }

    inner class EventViewHolder(
        private val binding: EventCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentEvent: EventItem? = null

        init {
            val navigateToProfileAction = View.OnClickListener {
                currentEvent?.let { event ->
                    onInteractionListener.onAuthorClick(event.authorId)
                }
            }
            binding.author.setOnClickListener(navigateToProfileAction)
            binding.avatar.setOnClickListener(navigateToProfileAction)

            binding.menuButton.setOnClickListener { view ->
                currentEvent?.let { event ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.options_post) //Используем тоже меню, что и для поста
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(event)
                                    true
                                }

                                R.id.edit -> {
                                    onInteractionListener.onEdit(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }

            binding.like.setOnClickListener {
                currentEvent?.let { event ->
                    onInteractionListener.onLike(event)
                }
            }

            binding.share.setOnClickListener {
                currentEvent?.let { event ->
                    onInteractionListener.onShare(event)
                }
            }
        }

        fun bind(event: EventItem) {
            currentEvent = event
            val isOwnedByMe = if (event.authorId == currentUserId) {
                true
            } else {
                false
            }

            binding.apply {

                author.text = event.author
                avatar.loadAvatar(event.authorAvatar, event.author)
                published.text = DateUtils.formatIsoDate(event.published)
                content.text = event.content

                like.isChecked = event.likedByMe
                like.text = "${event.likeOwnerIds?.size ?: 0}"


                menuButton.visibility = if (isOwnedByMe) View.VISIBLE else View.INVISIBLE

                val attachmentUrl = event.attachment?.url
                if (!attachmentUrl.isNullOrBlank()) {
                    attachment.loadAttachment(attachmentUrl)
                    attachment.visibility = View.VISIBLE
                } else {
                    attachment.visibility = View.GONE
                }
            }
        }
    }

    class EventItemDiffCallback : DiffUtil.ItemCallback<EventItem>() {
        override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem == newItem
        }
    }
}