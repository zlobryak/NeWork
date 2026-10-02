package ru.netology.nework.ui.adapters.usersFeed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.databinding.UserCardBinding
import ru.netology.nework.view.loadAvatar

/**
 * Адаптер полноэкранного списка пользователей (карточка: аватар + имя + логин).
 *
 * Используется вкладкой "Users" главного экрана, а также
 * для списков лайкнувших/упомянутых/спикеров/участников из детальных экранов.
 */
class UsersPagingAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<UserItem, UsersPagingAdapter.UserViewHolder>(USER_COMPARATOR) {

    interface OnInteractionListener {
        fun onUserClick(user: UserItem) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class UserViewHolder(
        private val binding: UserCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentUser: UserItem? = null

        init {
            // Клик по всей карточке ведёт в детальный вид пользователя
            binding.root.setOnClickListener {
                currentUser?.let(onInteractionListener::onUserClick)
            }
            binding.avatar.setOnClickListener {
                currentUser?.let(onInteractionListener::onUserClick)
            }
        }

        fun bind(user: UserItem) {
            currentUser = user
            binding.apply {
                name.text = user.name
                login.text = user.login
                avatar.loadAvatar(
                    url = user.avatar,
                    authorName = user.name?.ifEmpty { user.login },
                )
            }
        }
    }

    companion object {
        private val USER_COMPARATOR: DiffUtil.ItemCallback<UserItem> =
            object : DiffUtil.ItemCallback<UserItem>() {
                override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
                    oldItem == newItem
            }
    }
}
