package ru.netology.nework.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import ru.netology.nework.view.loadAvatar

class UserWallPostPagingAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<PostItem, UserWallPostPagingAdapter.PostViewHolder>(POST_COMPARATOR) {

    companion object {
        // DiffUtil говорит Paging 3, как сравнивать посты, чтобы не перерисовывать весь список
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<PostItem>() {
            override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
                oldItem == newItem
        }
    }

    interface OnInteractionListener {
        fun onLike(post: PostItem) {}
        fun onEdit(post: PostItem) {}
        fun onRemove(post: PostItem) {}
        fun onShare(post: PostItem) {}
        fun onAuthorClick(userId: Int) {}
    }

    inner class PostViewHolder(
        private val binding: PostCardBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostItem) {

            binding.apply {
                author.text = post.authorName
                avatar.loadAvatar(post.authorAvatar, post.authorName)
                // Создаем одно действие для клика по автору и аватару
                val navigateToProfileAction = View.OnClickListener {
                    // Замените post.authorId на реальное имя поля в вашем DTO
                    onInteractionListener.onAuthorClick(post.authorId)
                }
                author.setOnClickListener(navigateToProfileAction)
                avatar.setOnClickListener(navigateToProfileAction)

                published.text = DateUtils.formatIsoDate(post.published)
                content.text = post.content
                like.isChecked = post.likedByMe
                like.text = "${post.likeOwnerIds?.size}"

                menuButton.visibility =
                    if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

                menuButton.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        inflate(R.menu.options_post)
                        // TODO: if we don't have other options, just remove dots
                        // Здесь 'menu' ссылается на свойство PopupMenu, перекрывая внешний binding.menu
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

                like.setOnClickListener {
                    Log.d(
                        "LIKE_DEBUG",
                        "Клик по лайку! Post ID: ${post.id}, текущий likedByMe: ${post.likedByMe}"
                    )
                    onInteractionListener.onLike(post)
                }

                share.setOnClickListener {
                    onInteractionListener.onShare(post)
                }

                val attachmentUrl = post.attachment?.url
                if (!attachmentUrl.isNullOrBlank()) {
                    attachment.loadAttachment(attachmentUrl)
                    // Показываем блок с вложением
                    attachment.visibility = View.VISIBLE
                } else {
                    // Скрываем блок, вложения, если его нет
                    attachment.visibility = View.GONE
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // getItem(position) безопасно возвращает PostItem? (может быть null, если данные еще грузятся)
        val post = getItem(position)
        if (post != null) {
            holder.bind(post)
        }
    }
}