package ru.netology.nework.ui.adapters.usersFeed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.databinding.UserCardBinding
import ru.netology.nework.view.loadAvatar

/**
 * Адаптер полного (не обрезанного до 5 аватарок) списка пользователей.
 *
 * Используется во фрагменте UsersListFragment, который открывается
 * по кнопке "+" из детальных экранов поста и события
 * (списки лайкнувших, упомянутых, спикеров, участников).
 *
 * В отличие от UsersPagingAdapter работает с обычным List<UserItem>,
 * т.к. полный список уже известен и передаётся во фрагмент аргументом.
 */
class UsersListAdapter(
    private val onUserClick: (UserItem) -> Unit,
) : ListAdapter<UserItem, UsersListAdapter.UserViewHolder>(USER_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: UserCardBinding,
        private val onUserClick: (UserItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserItem) {
            binding.apply {
                name.text = user.name
                login.text = user.login
                avatar.loadAvatar(
                    url = user.avatar,
                    authorName = user.name?.ifEmpty { user.login },
                )

                root.setOnClickListener {
                    onUserClick(user)
                }
            }
        }
    }

    companion object {
        private val USER_COMPARATOR =
            object : DiffUtil.ItemCallback<UserItem>() {
                override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean =
                    oldItem == newItem
            }
    }
}
