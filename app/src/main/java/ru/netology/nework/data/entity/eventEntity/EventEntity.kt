package ru.netology.nework.data.entity.eventEntity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.AttachmentEmbeddable
import ru.netology.nework.data.entity.CoordsEmbeddable

@Entity(tableName = "EventEntity")
data class EventEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String?,
    val published: String,
    val datetime: String,
    val content: String?,

    // Для списков ID убедитесь, что в Converters есть методы List<Int> <-> String
    val type: String? = null,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    val participantsIds: List<Int>,
    val participatedByMe: Boolean,

    val speakerIds: List<Int>,
    @Embedded(prefix = "attachment_") //Решает проблему одинаковых названий в таблицах
    val attachment: AttachmentEmbeddable?,

    @Embedded(prefix = "coords_")
    val coords: CoordsEmbeddable? = null,
    val link: String? = null,
    val isDeleting: Boolean = false, // Для оптимистичного обновления UI при удалении
    val ownedByMe: Boolean,

    //Поля для локальной синхронизации
    val isSynced: Boolean = true,
    val syncStatus: String? = null,

    val users: Map<String, UserItem>? = null
) {
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
                speakerIds = dto.speakerIds,
                isDeleting = false,
                isSynced = dto.isSynced,
                syncStatus = dto.syncStatus,
                users = dto.users
            )
    }

    fun toDto() = EventItem(
        authorId,
        attachment?.toDto(),
        author,
        authorAvatar,
        authorJob,
        content,
        coords?.toDto(),
        datetime,
        id,
        likeOwnerIds,
        likedByMe,
        link,
        participantsIds,
        participatedByMe,
        published,
        speakerIds,
        type,
        users,
        isSynced,
        syncStatus
    )

    // Вспомогательные методы для получения пользователей по ID из поля users
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

fun List<EventEntity>.toDto(): List<EventItem> = map(EventEntity::toDto)
fun List<EventItem>.toEntity(currentUserId: Int?): List<EventEntity> =
    map { EventEntity.fromDto(it, currentUserId) }

//TODO Добавить обработку массива users для отображаения аватров