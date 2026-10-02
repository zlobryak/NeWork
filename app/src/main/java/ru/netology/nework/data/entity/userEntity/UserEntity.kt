package ru.netology.nework.data.entity.userEntity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.data.dto.user.UserItem

/**
 * Локальная копия пользователя.
 * API умеет отдавать только весь список пользователей сразу одним запросом,
 * поэтому мы сохраняем его целиком в БД и листаем локально (Room PagingSource).
 */
@Entity(tableName = "UserEntity")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int?,
    val avatar: String?,
    val login: String?,
    val name: String?,
) {
    fun toDto() = UserItem(
        id = id,
        avatar = avatar,
        login = login,
        name = name,
    )

    companion object {
        fun fromDto(dto: UserItem) = UserEntity(
            id = dto.id,
            avatar = dto.avatar,
            login = dto.login,
            name = dto.name,
        )
    }
}
