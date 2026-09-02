package ru.netology.nework.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.databinding.PostCardBinding

class PostPagingAdapter(
    private val onLike: (PostItem) -> Unit,
    private val onRemove: (PostItem) -> Unit,
    private val onShare: (PostItem) -> Unit,
    private val onEdit: (PostItem) -> Unit
) : PagingDataAdapter<PostItem, PostPagingAdapter.PostViewHolder>(POST_COMPARATOR) {

    companion object {
        // DiffUtil говорит Paging 3, как сравнивать посты, чтобы не перерисовывать весь список
        private val POST_COMPARATOR = object : DiffUtil.ItemCallback<PostItem>() {
            override fun areItemsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PostItem, newItem: PostItem): Boolean =
                oldItem == newItem
        }
    }

    inner class PostViewHolder(private val binding: PostCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: PostItem) {
            // TODO: Заполните binding данными из post
            // binding.postContent.text = post.content
            // binding.likeButton.setOnClickListener { onLike(post) }
            // и так далее...
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // getItem(position) безопасно возвращает PostItem? (может быть null, если данные еще грузятся)
        val post = getItem(position)
        if (post != null) {
            holder.bind(post)
        }
    }
}