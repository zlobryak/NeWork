package ru.netology.nework.data.repository.user

import ru.netology.nework.data.dto.user.UserItem

interface UserRepository {
    suspend fun getUser(userId: Int): UserItem
    /**
     * Пачка пользователей для пагинатора. Текущий API возвращает список всех пользователей.
     * @return список пользователей; пустой список означает, что элементы закончились
     */
    suspend fun loadUsersPage(): List<UserItem>
}