package ru.netology.nework.ui.adapters.postFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import ru.netology.nework.view.loadAvatar

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<PostItem, FeedAdapter.PostViewHolder>(FeedItemDiffCallback()) {

    interface OnInteractionListener {
        fun onLike(post: PostItem) {}
        fun onEdit(post: PostItem) {}
        fun onRemove(post: PostItem) {}
        fun onShare(post: PostItem) {}
        fun onAuthorClick(userId: Int) {}
    }

    // Упрощаем создание ViewHolder, убираем лишний кастинг
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let { post ->
            holder.bind(post)
        }
    }

    class PostViewHolder(
        private val binding: PostCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        // Храним ссылку на текущий пост, чтобы слушатели знали, с чем работать
        private var currentPost: PostItem? = null

        init {
            // Слушатели создаются при создании ячейки
            val navigateToProfileAction = View.OnClickListener {
                currentPost?.let { post ->
                    onInteractionListener.onAuthorClick(post.authorId)
                }
            }
            binding.author.setOnClickListener(navigateToProfileAction)
            binding.avatar.setOnClickListener(navigateToProfileAction)

            binding.menuButton.setOnClickListener { view ->
                currentPost?.let { post ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.options_post)
                        menu.setGroupVisible(R.id.owned, post.ownedByMe)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(post)
                                    true
                                }
                                R.id.edit -> {
                                    onInteractionListener.onEdit(post)
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }

            binding.like.setOnClickListener {
                currentPost?.let { post ->
                    onInteractionListener.onLike(post)
                }
            }

            binding.share.setOnClickListener {
                currentPost?.let { post ->
                    onInteractionListener.onShare(post)
                }
            }
        }

        // Метод bind обновляет данные, не создавая объектов
        fun bind(post: PostItem) {
            currentPost = post // Обновляем ссылку для слушателей

            binding.apply {
                author.text = post.authorName
                avatar.loadAvatar(post.authorAvatar, post.authorName)
                published.text = DateUtils.formatIsoDate(post.published)
                content.text = post.content

                like.isChecked = post.likedByMe
                like.text = "${post.likeOwnerIds?.size ?: 0}" // Защита от null

                menuButton.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

                val attachmentUrl = post.attachment?.url
                if (!attachmentUrl.isNullOrBlank()) {
                    attachment.loadAttachment(attachmentUrl)
                    attachment.visibility = View.VISIBLE
                } else {
                    attachment.visibility = View.GONE
                }
            }
        }
    }

    class FeedItemDiffCallback : DiffUtil.ItemCallback<PostItem>() {
        override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
            oldItem == newItem
    }
}