package ru.netology.nework.data.repository.post

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.WallApiService
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.dao.UserWallRemoteKeyDao
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.data.entity.UserWallRemoteKeyEntity
import ru.netology.nework.data.entity.toEntity
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class UserWallRemoteMediator @Inject constructor(
    private val authorId: Int,
    private val wallApiService: WallApiService,
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val userWallRemoteKeyDao: UserWallRemoteKeyDao,
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            // Определяем, какой метод API вызывать в зависимости от типа загрузки
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    // При обновлении всегда запрашиваем самые свежие посты
                    wallApiService.getLatestWallPosts(
                        authorId = authorId,
                        count = state.config.pageSize
                    )
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                LoadType.APPEND -> {
                    // При загрузке вниз получаем ключ (id последнего поста)
                    val remoteKey = userWallRemoteKeyDao.getRemoteKeyForAuthor(
                        authorId = authorId,
                        type = UserWallRemoteKeyEntity.KeyType.AFTER
                    )

                    // Если ключа нет, значит нечего загружать (или это первый запуск, который должен быть REFRESH)
                    if (remoteKey == null || remoteKey.nextKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    // Вызываем метод /after, передавая id последнего известного поста
                    wallApiService.getAfter(
                        authorId = authorId,
                        id = remoteKey.nextKey,
                        count = state.config.pageSize
                    )
                }
            }

            // 2Обработка ответа
            if (!response.isSuccessful) {
                return MediatorResult.Error(Exception("HTTP ${response.code()} ${response.message()}"))
            }

            val posts = response.body() ?: emptyList()
            val endOfPaginationReached = posts.isEmpty()

            // Сохранение в БД в транзакции
            appDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    // Очищаем только посты и ключи конкретного автора
                    postDao.clearPostsByAuthorId(authorId)
                    userWallRemoteKeyDao.removeKeysForAuthor(authorId)
                }

                val entities = posts.toEntity(authorId)
                postDao.insert(entities)

                //  Сохраняем ключ для следующей пагинации, если пришли данные
                if (!endOfPaginationReached) {
                    val nextKeyValue = posts.lastOrNull()?.id

                    if (nextKeyValue != null) {
                        userWallRemoteKeyDao.insert(
                            UserWallRemoteKeyEntity(
                                authorId = authorId,
                                type = UserWallRemoteKeyEntity.KeyType.AFTER,
                                nextKey = nextKeyValue
                            )
                        )
                    }
                }
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("WALL_MEDIATOR_ERROR", "Ошибка загрузки в UserWallRemoteMediator", e)
            MediatorResult.Error(e)
        }
    }
}