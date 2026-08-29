package ru.netology.nework.data.dto.event

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.user.Users

@Parcelize
data class EventItem(
    val attachment: Attachment,
    val author: String,
    val authorAvatar: String,
    val authorId: Int,
    val authorJob: String,
    val content: String,
    val coords: Coords,
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
    val users: Users?
) : Parcelable