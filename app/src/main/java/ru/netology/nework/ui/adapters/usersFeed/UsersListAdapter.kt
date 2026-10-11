package ru.netology.nework.ui.adapters.usersFeed

import android.view.LayoutInflater
import android.view.View
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
 * (списки лайкнувших, упомянутых, спикеров, участников),
 * а также в режиме ВЫБОРА при создании поста/события
 * (упомянуть пользователя / пригласить спикера).
 *
 * В отличие от UsersPagingAdapter работает с обычным List<UserItem>,
 * т.к. полный список уже известен и передаётся во фрагмент аргументом.
 */
class UsersListAdapter(
    // Режим выбора: клик по карточке отмечает пользователя чекбоксом,
    // а не открывает его профиль.
    private val selectionMode: Boolean = false,
    initiallySelected: Set<Int> = emptySet(),
    private val onSelectionChanged: (Set<Int>) -> Unit = {},
    private val onUserClick: (UserItem) -> Unit = {},
) : ListAdapter<UserItem, UsersListAdapter.UserViewHolder>(USER_COMPARATOR) {

    // Id отмеченных пользователей переживают пересоздание фрагмента,
    // поэтому хранятся в самом адаптере (восстанавливаются из savedStateHandle)
    private val selectedIds: MutableSet<Int> = initiallySelected.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = UserCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: UserCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserItem) {
            binding.apply {
                name.text = user.name
                login.text = user.login
                avatar.loadAvatar(
                    url = user.avatar,
                    authorName = user.name?.ifEmpty { user.login },
                )

                selectionCheckbox.visibility =
                    if (selectionMode) View.VISIBLE else View.GONE
                selectionCheckbox.setOnCheckedChangeListener(null)
                selectionCheckbox.isChecked = selectedIds.contains(user.id)

                root.setOnClickListener {
                    if (selectionMode) {
                        // Переключение отметки + синхронизация чекбокса
                        val nowChecked = !selectedIds.contains(user.id)
                        if (nowChecked) selectedIds.add(user.id) else selectedIds.remove(user.id)
                        selectionCheckbox.isChecked = nowChecked
                        onSelectionChanged(selectedIds.toSet())
                    } else {
                        onUserClick(user)
                    }
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
