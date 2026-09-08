package ru.netology.nework.data.repository.post

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.data.entity.PostRemoteKeyEntity
import ru.netology.nework.data.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.data.dao.PostRemoteKeyDao
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val service: ApiService,
    private val db: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val auth: AppAuth
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val response = when (loadType) {
                LoadType.REFRESH -> service.getLatest(state.config.initialLoadSize)

                LoadType.PREPEND -> {
                    // Ищем ключ для подгрузки НОВЫХ постов
                    val id = postRemoteKeyDao.getKey(PostRemoteKeyEntity.KeyType.AFTER)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    service.getAfter(id.toLong(), state.config.pageSize)
                }

                LoadType.APPEND -> {
                    // Ищем ключ для подгрузки СТАРЫХ постов
                    val id = postRemoteKeyDao.getKey(PostRemoteKeyEntity.KeyType.BEFORE)
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                    service.getBefore(id.toLong(), state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            // Если сервер вернул пустой список, значит лента закончилась
            if (body.isEmpty()) {
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            db.withTransaction {
                val currentUserId = auth.authStateFlow.value.id.toInt()
                when (loadType) {
                    LoadType.REFRESH -> {
                        postRemoteKeyDao.removeAll()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id, // Самый новый ID
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id,  // Самый старый ID
                                ),
                            )
                        )
                        postDao.removeAll()
                    }

                    LoadType.PREPEND -> {
                        // Обновляем ключ для новых постов
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            )
                        )
                    }

                    LoadType.APPEND -> {
                        // Обновляем ключ для старых постов
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                    }
                }
                postDao.insert(body.toEntity(currentUserId))
            }

            return MediatorResult.Success(endOfPaginationReached = false)

        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("POST_MEDIATOR_ERROR", "Ошибка загрузки в PostRemoteMediator", e)
            return MediatorResult.Error(e)
        }
    }
}