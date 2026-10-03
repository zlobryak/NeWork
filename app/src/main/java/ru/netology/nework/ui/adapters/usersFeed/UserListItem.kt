package ru.netology.nework.ui.adapters.usersFeed

sealed class UserListItem {
    data class User(
        val userId: Int?,
        val avatarUrl: String?,
        val name: String? = "",
    ) : UserListItem()

    data object AddButton : UserListItem()
}