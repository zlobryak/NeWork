package ru.netology.nework.data.entity.eventEntity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.entity.AttachmentEmbeddable
import ru.netology.nework.data.entity.CoordsEmbeddable

@Entity(tableName = "EventEntity")
data class EventEntity(
    @PrimaryKey val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String,
    val authorJob: String,
    val published: String,
    val datetime: String,
    val content: String,

    // Для списков ID убедитесь, что в Converters есть методы List<Int> <-> String
    val type: String,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val participantsIds: List<Int>,
    val participatedByMe: Boolean,

    val speakerIds: List<Int>,
    @Embedded(prefix = "attachment_") //Решает проблему одинаковых названий в таблицах
    val attachment: AttachmentEmbeddable?,

    @Embedded(prefix = "coords_")
    val coords: CoordsEmbeddable?,
    val link: String,
    val isDeleting: Boolean = false, // Для оптимистичного обновления UI при удалении){}
    val ownedByMe: Boolean
){
    companion object {
        fun fromDto(dto: EventItem, currentUserId: Int?) =
            EventEntity(
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
                published = dto.published,
                ownedByMe = if (currentUserId != null) (dto.authorId == currentUserId) else false,
                datetime = dto.datetime,
                type = dto.type,
                participantsIds = dto.participantsIds,
                participatedByMe = dto.participatedByMe,
                speakerIds = dto.participantsIds,
                isDeleting = false,
            )

    }
}