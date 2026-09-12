package ru.netology.nework.data.repository.user

import ru.netology.nework.data.dto.user.UserItem

interface UserRepository {
    suspend fun getUser(userId: Int): UserItem
}