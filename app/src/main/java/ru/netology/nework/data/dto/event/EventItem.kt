package ru.netology.nework.data.dto.event

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.user.UserItem

@Parcelize
data class EventItem(
    val authorId: Int,
    val attachment: Attachment?,
    val author: String,
    val authorAvatar: String,
    val authorJob: String,
    val content: String,
    val coords: Coords?,
    val datetime: String,
    val id: Int,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val link: String,
    val participantsIds: List<Int>,
    val participatedByMe: Boolean,
    val published: String,
    val speakerIds: List<Int>,
    val type: String,
    val users: Map<String, UserItem>? = null,

    val isSynced: Boolean = true, //Локальные поля для удаления/восстановления
    val syncStatus: String? = null
) : Parcelable {
    fun getUserById(id: Int): UserItem? = users?.get(id.toString())

    fun getSpeakers(): List<UserItem> = speakerIds.mapNotNull { id ->
        users?.get(id.toString())
    }

    fun getParticipants(): List<UserItem> = participantsIds.mapNotNull { id ->
        users?.get(id.toString())
    }

    fun getLikers(): List<UserItem> = likeOwnerIds.mapNotNull { id ->
        users?.get(id.toString())
    }
}
