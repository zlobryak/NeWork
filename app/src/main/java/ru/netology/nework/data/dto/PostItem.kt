package ru.netology.nework.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostItem(
    val id: Int,
    val attachment: Attachment?,
    val author: String,
    val authorAvatar: String?,
    val authorId: Int,
    val authorJob: String?,
    val content: String?,
    val coords: Coords?,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<Int>?,
    val mentionedMe: Boolean,
    val published: String,
    val users: Users?,
    @Transient
    val ownedByMe: Boolean,
    @Transient
    val isDeleting: Boolean = false

): Parcelable