package ru.netology.nework.ui.adapters.eventDetailFragmment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R

class EventUsersAdapter(
    private val onItemClick: (String?) -> Unit,
    private val onMoreButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<EventUserListItem>()

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BUTTON = 2
        private const val MAX_USERS = 5
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is EventUserListItem.User -> TYPE_USER
            is EventUserListItem.AddButton -> TYPE_BUTTON
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
                val user = items[position] as EventUserListItem.User
                holder.bind(user)
            }
            is AddButtonViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(users: List<EventUserListItem.User>) {
        items.clear()

        // Добавляем максимум 5 пользователей
        val usersToShow = users.take(MAX_USERS)
        items.addAll(usersToShow)

        // Если пользователей больше 5, добавляем кнопку "+"
        if (users.size > MAX_USERS) {
            items.add(EventUserListItem.AddButton)
        }

        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.user_avatar)

        fun bind(user: EventUserListItem.User) {
            // Здесь загрузка аватара (Glide/Picasso)
            // Glide.with(itemView).load(user.avatarUrl).into(imageView)
            imageView.setImageResource(R.drawable.ic_manufacturing_24px) // заглушка
            imageView.setOnClickListener { onItemClick(user.userId) }
        }
    }

    inner class AddButtonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            itemView.setOnClickListener { onMoreButtonClick() }
        }
    }
}