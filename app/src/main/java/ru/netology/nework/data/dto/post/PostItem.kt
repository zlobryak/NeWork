package ru.netology.nework.data.dto.post

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.PostEntity

@Parcelize
data class PostItem(
    val id: Int,
    val attachment: Attachment?,
    val authorName: String,
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
    val users: Map<String, UserItem>? = null,
    @Transient
    val ownedByMe: Boolean,
    @Transient
    val isDeleting: Boolean = false,
    @Transient
    val isSynced: Boolean,
    @Transient
    val syncStatus: PostEntity.SyncStatus? = null,

    ): Parcelable