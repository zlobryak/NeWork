package ru.netology.nework.data.repository.events

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.data.dto.event.EventItem

interface EventRepository {
    // Поток для общей ленты событий с пагинацией
    val getAllEventsData: Flow<PagingData<EventItem>>

    fun getEventById(id: Int): Flow<EventItem?>

    // Базовые операции
    suspend fun save(event: EventItem)
    suspend fun removeById(id: Int)
    suspend fun likeEvent(id: Int, likedByMe: Boolean)
    suspend fun restoreEvent(event: EventItem)
}