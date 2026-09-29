package ru.netology.nework.data.dto.post

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.postEntity.PostEntity

@Parcelize
data class PostItem(
    val id: Int,
    val attachment: Attachment?,
    @SerializedName("author") //Добавил эту аннтоцию для проверки. В gson это поле называется не так как в классе
    val authorName: String?,
    val authorAvatar: String?,
    val authorId: Int,
    val authorJob: String?, //Только последняя работа?
    val content: String?,
    val coords: Coords?,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<Int> = emptyList(),
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

    ) : Parcelable {
    fun getLikers(): List<UserItem> = likeOwnerIds.mapNotNull { id ->
        users?.get(id.toString())
    }

    fun getMentioned(): List<UserItem> = mentionIds.mapNotNull { id ->
        users?.get(id.toString())
    }
}