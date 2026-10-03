package ru.netology.nework.data.repository.user

import androidx.paging.PagingSource
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.userEntity.UserEntity

interface UserRepository {
    suspend fun getUser(userId: Int): UserItem
    /**
     * Пачка пользователей для пагинатора. Текущий API возвращает список всех пользователей.
     * @return список пользователей; пустой список означает, что элементы закончились
     */
    suspend fun loadUsersPage(): List<UserItem>

    /**
     * Источник страниц для Pager'а — локальная таблица со всеми пользователями.
     * Пагинация идёт по БД, а не по сети: API отдаёт только весь список целиком
     * одним запросом GET /users без возможности разбить его на пачки.
     */
    fun pagingSource(): PagingSource<Int, UserEntity>

    /** Полная перезагрузка списка пользователей с сервера */
    suspend fun refreshUsers()

    /** Синхронизация при первом открытии: скачать всех, если БД пуста. */
    suspend fun syncIfNeeded()
}