package ru.netology.nework.data.repository.post

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nework.api.WallApiService
import ru.netology.nework.auth.AppAuth
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
    private val auth: AppAuth
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = userWallRemoteKeyDao.getRemoteKeyForAuthor(authorId)
                    remoteKey?.nextKey
                }
            }

            // ИСПРАВЛЕНИЕ 1: Используем getWallPosts с параметром key, а не getLatest.
            // getLatest игнорирует ключ пагинации и всегда возвращает первые посты.
            val response = wallApiService.getWallPosts(
                authorId = authorId,
                key = loadKey,
                count = state.config.pageSize
            )

            if (!response.isSuccessful) {
                return MediatorResult.Error(Exception("HTTP ${response.code()} ${response.message()}"))
            }

            val posts = response.body() ?: emptyList()
            val endOfPaginationReached = posts.isEmpty()

            appDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDao.clearPostsByAuthorId(authorId)
                    userWallRemoteKeyDao.removeKeysForAuthor(authorId)
                }

                // ИСПРАВЛЕНИЕ 2: Вызываем toEntity на всем списке posts, а не через map.
                // Это вернет List<PostEntity>, что идеально подходит для postDao.insert
                val entities = posts.toEntity(authorId)
                postDao.insert(entities)

                if (!endOfPaginationReached) {
                    val nextKeyValue = posts.lastOrNull()?.id

                    // ИСПРАВЛЕНИЕ 3: Используем KeyType.NEXT для консистентности
                    // (так как в DAO по умолчанию запрашивается именно NEXT)
                    userWallRemoteKeyDao.insert(
                        UserWallRemoteKeyEntity(
                            authorId = authorId,
                            type = UserWallRemoteKeyEntity.KeyType.NEXT,
                            nextKey = nextKeyValue
                        )
                    )
                }
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            Log.e("WALL_MEDIATOR_ERROR", "Ошибка загрузки в UserWallRemoteMediator", e)
            MediatorResult.Error(e)
        }
    }
}