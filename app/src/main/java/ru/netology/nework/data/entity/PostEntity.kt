package ru.netology.nework.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.data.dto.Users
import kotlin.String

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    val author: String,
    val authorAvatar: String?,
    val authorId: Int,
    val authorJob: String?,
    val content: String?,
    @Embedded
    val coords: CoordsEmbeddable?,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<Int>?,
    val mentionedMe: Boolean,
    val published: String,
    val users: Users?,
    val ownedByMe: Boolean,
    val isDeleting: Boolean = false,
    val isSynced: Boolean,
    val syncStatus: SyncStatus,

) {
    fun toDto() = PostItem(
        id,
        attachment?.toDto(),
        author,
        authorAvatar,
        authorId,
        authorJob,
        content,
        coords?.toDto(),
        likeOwnerIds,
        likedByMe,
        link,
        mentionIds,
        mentionedMe,
        published,
        users,
        ownedByMe,
        isDeleting,
        isSynced,
    )

    companion object {
        fun fromDto(dto: PostItem, currentUserId: Int?) =
            PostEntity(
                id = dto.id,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorId = dto.authorId,
                authorJob = dto.authorJob,
                content = dto.content,
                coords = CoordsEmbeddable.fromDto(dto.coords),
                likeOwnerIds = dto.likeOwnerIds,
                likedByMe = dto.likedByMe,
                link = dto.link,
                mentionIds = dto.mentionIds,
                mentionedMe = dto.mentionedMe,
                published = dto.published,
                users = dto.users,
                ownedByMe = if (currentUserId != null) (dto.authorId == currentUserId) else false,
                isDeleting = false, //Локальное поле, по умолчанию всегда false
                isSynced = true,
                syncStatus = SyncStatus.SYNCED,
            )

    }

    enum class SyncStatus {
        PENDING,
        SYNCED,
        FAILED
    }
}

fun List<PostEntity>.toDto(): List<PostItem> = map(PostEntity::toDto)
fun List<PostItem>.toEntity(currentUserId: Int?): List<PostEntity> =
    map { PostEntity.fromDto(it, currentUserId) }
