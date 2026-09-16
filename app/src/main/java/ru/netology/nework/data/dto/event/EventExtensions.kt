package ru.netology.nework.data.dto.event

import ru.netology.nework.data.dto.user.UserItem

/**
 * Проверяет, лайкнул ли текущий пользователь событие.
 * Работает через список likeOwnerIds, что является single source of truth.
 */
fun EventItem.isLikedBy(userId: Long): Boolean =
    userId != 0L && likeOwnerIds.contains(userId.toInt())

/**
 * Проверяет, участвует ли текущий пользователь в событии.
 * Работает через список participantsIds, что является single source of truth.
 */
fun EventItem.isParticipating(userId: Long): Boolean =
    userId != 0L && participantsIds.contains(userId.toInt())

/**
 * Вспомогательные методы для получения пользователей по их ID.
 * Используют поле users (Map<String, UserItem>) для резолва.
 */
fun EventItem.getSpeakers(): List<UserItem> =
    speakerIds.mapNotNull { id -> users?.get(id.toString()) }

fun EventItem.getLikers(): List<UserItem> =
    likeOwnerIds.mapNotNull { id -> users?.get(id.toString()) }

fun EventItem.getParticipants(): List<UserItem> =
    participantsIds.mapNotNull { id -> users?.get(id.toString()) }