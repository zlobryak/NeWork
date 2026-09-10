package ru.netology.nework.ui.adapters.eventFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.event.eventItem
import ru.netology.nework.databinding.eventCardBinding
import ru.netology.nework.ui.adapters.eventFeed.FeedAdapter
import ru.netology.nework.ui.adapters.eventFeed.FeedAdapter.FeedItemDiffCallback
import ru.netology.nework.ui.adapters.eventFeed.FeedAdapter.eventViewHolder
import ru.netology.nework.ui.adapters.postFeed.FeedAdapter
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import ru.netology.nework.view.loadAvatar


class EventsPagingAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<EventItem, FeedAdapter.EventViewHolder>(FeedAdapter.FeedItemDiffCallback()) {

    interface OnInteractionListener {
        fun onLike(event: eventItem) {}
        fun onEdit(event: eventItem) {}
        fun onRemove(event: eventItem) {}
        fun onShare(event: eventItem) {}
        fun onAuthorClick(userId: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = eventCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return eventViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let { event ->
            holder.bind(event)
        }
    }

    class EventViewHolder(
        private val binding: eventCardBinding,
        private val onInteractionListener: FeedAdapter.OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        // Храним ссылку на текущий пост, чтобы слушатели знали, с чем работать
        private var currentEvent: EventItem? = null

        init {
            // Слушатели создаются при создании ячейки
            val navigateToProfileAction = View.OnClickListener {
                currentevent?.let { event ->
                    onInteractionListener.onAuthorClick(event.authorId)
                }
            }
            binding.author.setOnClickListener(navigateToProfileAction)
            binding.avatar.setOnClickListener(navigateToProfileAction)

            binding.menuButton.setOnClickListener { view ->
                currentEvent?.let { event ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.options_post)
                        menu.setGroupVisible(R.id.owned, event.ownedByMe)
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

        // Метод bind обновляет данные, не создавая объектов
        fun bind(event: EventItem) {
            currentEvent = event // Обновляем ссылку для слушателей

            binding.apply {
                author.text = event.authorName
                avatar.loadAvatar(event.authorAvatar, event.authorName)
                published.text = DateUtils.formatIsoDate(event.published)
                content.text = event.content

                like.isChecked = event.likedByMe
                like.text = "${event.likeOwnerIds?.size ?: 0}" // Защита от null

                menuButton.visibility = if (event.ownedByMe) View.VISIBLE else View.INVISIBLE

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
}
//todo