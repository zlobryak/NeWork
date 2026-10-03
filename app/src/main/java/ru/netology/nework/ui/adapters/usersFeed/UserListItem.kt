package ru.netology.nework.ui.adapters.usersFeed

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.netology.nework.data.dto.user.UserItem

@Suppress("DEPRECATED_ANNOTATION")
@Parcelize
sealed class UserListItem : Parcelable {
    data class User(
        val userId: Int?,
        val avatarUrl: String?,
        val name: String? = "",
    ) : UserListItem(), Parcelable

    data object AddButton : UserListItem()

    /**
     * Приводит укороченную модель из ряда аватарок к полноценному UserItem
     * для отображения в карточке полного списка.
     * Поля userId/login в ряду аватарок не хранятся — берём дефолты.
     */
    fun User.toUserItem(): UserItem =
        UserItem(
            id = userId ?: 0,
            avatar = avatarUrl,
            name = name,
        )
}