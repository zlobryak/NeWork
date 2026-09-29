package ru.netology.nework.ui.adapters.users

sealed class UserListItem {
    data class User(
        val userId: String?,
        val avatarUrl: String?,
        val name: String? = "",
    ) : UserListItem()

    data object AddButton : UserListItem()
}