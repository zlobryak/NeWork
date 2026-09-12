package ru.netology.nework.data.repository.events

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.api.EventApiService
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.entity.eventEntity.EventEntity
import ru.netology.nework.data.entity.eventEntity.toEntity
import ru.netology.nework.data.dao.eventDao.EventDao
import ru.netology.nework.data.dao.eventDao.EventRemoteKeyDao
import ru.netology.nework.data.entity.eventEntity.EventRemoteKeyEntity
import ru.netology.nework.error.ApiError
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator @Inject constructor(
    private val service: EventApiService,
    private val db: AppDb,
    private val eventDao: EventDao,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val auth: AppAuth
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> 1

                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

                LoadType.APPEND -> {
                    val remoteKey = eventRemoteKeyDao.getRemoteKey()
                    if (remoteKey?.endOfPaginationReached == true) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    remoteKey?.nextPage ?: 1
                }
            }

            val response = service.getEvents(count = state.config.pageSize)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val events = response.body() ?: throw ApiError(response.code(), response.message())
            val endOfPaginationReached = events.isEmpty()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    eventRemoteKeyDao.clear()
                    eventDao.clearAll() // Убедись, что в EventDao есть метод clearAll() или removeAll()
                }

                val currentUserId = auth.authStateFlow.value.id.toInt()
                val entities = events.toEntity(currentUserId)

                eventDao.insert(entities)

                // Сохраняем новый глобальный ключ
                eventRemoteKeyDao.insertOrUpdate(
                    EventRemoteKeyEntity(
                        nextPage = if (endOfPaginationReached) null else currentPage + 1,
                        endOfPaginationReached = endOfPaginationReached
                    )
                )
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("EVENT_MEDIATOR_ERROR", "Ошибка загрузки в EventRemoteMediator", e)
            return MediatorResult.Error(e)
        }
    }
}