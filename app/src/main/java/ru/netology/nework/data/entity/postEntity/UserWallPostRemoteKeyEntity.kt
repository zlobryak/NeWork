package ru.netology.nework.data.entity.postEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Используем отдельную таблицу, чтобы не конфликтовать с общей лентой
@Entity(tableName = "user_wall_remote_keys")
data class UserWallPostRemoteKeyEntity(
    @PrimaryKey
    val authorId: Int, // Первичный ключ - ID пользователя. У каждого юзера своя запись.
    val type: KeyType = KeyType.AFTER,
    val nextKey: Int? = null // Значение ключа (ID последнего поста)
) {
    enum class KeyType {
        AFTER, BEFORE
    }
}