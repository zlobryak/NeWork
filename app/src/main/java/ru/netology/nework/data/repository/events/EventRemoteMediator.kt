package ru.netology.nework.data.repository.events

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.api.EventApiService
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.entity.eventEntity.EventEntity
import ru.netology.nework.data.entity.eventEntity.toEntity
import ru.netology.nework.data.dao.eventDao.EventDao
import ru.netology.nework.data.dao.eventDao.EventRemoteKeyDao
import ru.netology.nework.data.entity.eventEntity.EventRemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
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
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = eventRemoteKeyDao.getRemoteKeyByEventId()
                    if (remoteKey?.endOfPaginationReached == true) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }
                    (remoteKey?.nextPage ?: 1)
                }
            }

            val response = service.getEvents(page = currentPage, limit = state.config.pageSize)

            if (!response.isSuccessful) {
                return MediatorResult.Error(Exception("HTTP ${response.code()}"))
            }

            val events = response.body() ?: emptyList()
            val endOfPaginationReached = events.isEmpty() // Или логика твоего API

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    eventRemoteKeyDao.clear()
                    eventDao.clearAll()
                }

                val currentUserId = auth.authStateFlow.value.id.toInt()
                val entities = events.map { it.toEntity(currentUserId) }

                eventDao.insertAll(entities)

                eventRemoteKeyDao.insertOrUpdate(
                    EventRemoteKeyEntity(
                        nextPage = if (endOfPaginationReached) null else currentPage + 1,
                        endOfPaginationReached = endOfPaginationReached
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}

//TODO Починить медиатор