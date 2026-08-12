package ru.netology.nework.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.BuildConfig
import ru.netology.nework.R
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.view.loadCircleCrop
import ru.netology.nework.data.dto.PostItem

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<PostItem, RecyclerView.ViewHolder>(FeedItemDiffCallback()) {

    interface OnInteractionListener {
        fun onLike(post: PostItem) {}
        fun onEdit(post: PostItem) {}
        fun onRemove(post: PostItem) {}
        fun onShare(post: PostItem) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("ADAPTER_DEBUG", " onCreateViewHolder вызван! RecyclerView запрашивает новую ячейку.")
        val layoutInflater = LayoutInflater.from(parent.context)
        return PostViewHolder(
            PostCardBinding.inflate(layoutInflater, parent, false),
            onInteractionListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("ADAPTER_DEBUG", "🔗 onBindViewHolder для позиции $position. der для позиции $position.)")
        getItem(position)?.let { post ->
            (holder as PostViewHolder).bind(post)
        }
    }

    class PostViewHolder(
        private val binding: PostCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: PostItem) {
            binding.apply {
                author.text = post.author
                published.text = post.published.toString()
                content.text = post.content
                avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")
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
                    onInteractionListener.onLike(post)
                }

                share.setOnClickListener {
                    onInteractionListener.onShare(post)
                }
            }
        }
    }

    class FeedItemDiffCallback : DiffUtil.ItemCallback<PostItem>() {
        override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem): Boolean {
            return oldItem == newItem
        }
    }
}

//TODO FinishRefactor