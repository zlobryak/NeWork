package ru.netology.nework.data.repository.events

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.api.EventApiService
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.entity.eventEntity.EventEntity
import ru.netology.nework.data.dao.eventDao.EventDao
import ru.netology.nework.data.dao.eventDao.EventRemoteKeyDao
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val appDb: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val eventApiService: EventApiService,
    private val auth: AppAuth
) : EventRepository {
    private val currentUserId: Int
        get() = auth.authStateFlow.value.id.toInt()

    @OptIn(ExperimentalPagingApi::class)
    override val getAllEventsData: Flow<PagingData<EventItem>> = Pager(
        config = PagingConfig(pageSize = 15),
        remoteMediator = EventRemoteMediator(
            service = eventApiService,
            db = appDb,
            eventDao = eventDao,
            eventRemoteKeyDao = eventRemoteKeyDao,
            auth = auth
        ),
        pagingSourceFactory = eventDao::pagingSource
    ).flow.map { pagingData ->
        pagingData.map(EventEntity::toDto)
    }

    override suspend fun save(event: EventItem) {
        try {
            val response = eventApiService.createEvent(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            eventDao.insert(EventEntity.fromDto(body, currentUserId))
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Int) {
        var success = false
        try {
            eventDao.markAsDeleting(id, true)
            val response = eventApiService.deleteEvent(id)
            if (response.isSuccessful) {
                success = true
                eventDao.removeById(id)
            } else {
                throw ApiError(response.code(), response.message())
            }
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        } finally {
            if (!success) {
                eventDao.markAsDeleting(id, false)
            }
        }
    }

    override suspend fun likeEvent(id: Int, likedByMe: Boolean) {
        try {
            val dbEvent = eventDao.getEventById(id)
            val isLikedByMe = dbEvent?.likedByMe ?: likedByMe

            val response = if (isLikedByMe) {
                eventApiService.dislikeEvent(id)
            } else {
                eventApiService.likeEvent(id)
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val updatedEvent = response.body()
                ?: throw ApiError(response.code(), "Пустой ответ от сервера при обновлении лайка")

            eventDao.insert(EventEntity.fromDto(updatedEvent, currentUserId))

        } catch (e: ApiError) {
            Log.e("EventLikeDebug", "Пробрасываем ApiError во ViewModel", e)
            throw e
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun restoreEvent(event: EventItem) {
        if (event.isSynced) {
            eventDao.insert(EventEntity.fromDto(event, currentUserId))
        } else {
            event.syncStatus?.let {
                eventDao.insert(
                    EventEntity.fromDto(event, currentUserId).copy(
                        isSynced = false,
                        syncStatus = it
                    )
                )
            }
        }
    }
}