package ru.netology.nework.ui.adapters.eventDetailFragmment

sealed class EventUserListItem {
    data class User(
        val userId: String?,
        val avatarUrl: String?,
        val name: String? = "",
    ) : EventUserListItem()

    data object AddButton : EventUserListItem()
}