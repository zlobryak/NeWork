package ru.netology.nework.ui.adapters.usersFeed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.view.loadAvatar

class UsersAdapter(
    private val onItemClick: (Int?) -> Unit,
    private val onMoreButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<UserListItem>()

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BUTTON = 2
        private const val MAX_USERS = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is UserListItem.User -> TYPE_USER
            is UserListItem.AddButton -> TYPE_BUTTON
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_user_avatar, parent, false)
                UserViewHolder(view)
            }
            TYPE_BUTTON -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_more_button, parent, false)
                AddButtonViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val user = items[position] as UserListItem.User
                holder.bind(user)
            }
            is AddButtonViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(users: List<UserListItem.User>) {
        items.clear()

        // Добавляем максимум 5 пользователей
        val usersToShow = users.take(MAX_USERS)
        items.addAll(usersToShow)

        // Если пользователей больше 5, добавляем кнопку "+"
        if (users.size > MAX_USERS) {
            items.add(UserListItem.AddButton)
        }

        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.user_avatar)

        fun bind(user: UserListItem.User) {
            imageView.loadAvatar(
                url = user.avatarUrl,
                authorName = user.name?.ifEmpty { user.userId.toString() }
            )
            imageView.setOnClickListener { onItemClick(user.userId) }
        }
    }

    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener { onMoreButtonClick() }
        }
    }
}