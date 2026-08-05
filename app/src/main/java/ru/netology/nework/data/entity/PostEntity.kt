package ru.netology.nework.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.data.dto.Users
import kotlin.String

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @Embedded
    var attachment: AttachmentEmbeddable?, val author: String,
    val authorAvatar: String,
    val authorId: Int,
    val authorJob: String?,
    val content: String?,
    @Embedded
    val coords: Coords?,
    val likeOwnerIds: List<Int>?,
    val likedByMe: Boolean,
    val link: String?,
    val mentionIds: List<Int>?,
    val mentionedMe: Boolean,
    val published: String,
    val users: Users?

) {
    fun toDto() = PostItem(
        id,
        attachment?.toDto(),
        author,
        authorAvatar,
        authorId,
        authorJob,
        content,
        coords,
        likeOwnerIds,
        likedByMe,
        link,
        mentionIds,
        mentionedMe,
        published,
        users
    )

    companion object {
        fun fromDto(dto: PostItem) =
            PostEntity(
                id = dto.id,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorId = dto.authorId,
                authorJob = dto.authorJob,
                content = dto.content,
                coords = dto.coords,
                likeOwnerIds = dto.likeOwnerIds,
                likedByMe = dto.likedByMe,
                link = dto.link,
                mentionIds = dto.mentionIds,
                mentionedMe = dto.mentionedMe,
                published = dto.published,
                users = dto.users,
            )

    }
}

fun List<PostEntity>.toDto(): List<PostItem> = map(PostEntity::toDto)
fun List<PostItem>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)


//TODO Файл скопирован, нужно переработать
