package ru.netology.nework.data.repository.events

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.post.Media
import ru.netology.nework.data.dto.post.MediaUpload

interface EventRepository {
    // Поток для общей ленты событий с пагинацией
    val getAllEventsData: Flow<PagingData<EventItem>>

    fun getEventById(id: Int): Flow<EventItem?>

    suspend fun save(event: EventItem, mediaUpload: MediaUpload?)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun removeById(id: Int)
    suspend fun likeEvent(id: Int, likedByMe: Boolean)

    suspend fun participateEvent(id: Int, participatedByMe: Boolean)
    suspend fun restoreEvent(event: EventItem)
}