package ru.netology.nework.data.repository.post

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.api.WallApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.dto.post.Media
import ru.netology.nework.data.dto.post.MediaUpload
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.dao.PostRemoteKeyDao
import ru.netology.nework.data.dao.UserWallRemoteKeyDao
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.AttachmentType
import ru.netology.nework.data.entity.toEntity
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val appDb: AppDb,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val userWallRemoteKeyDao: UserWallRemoteKeyDao,
    private val apiService: ApiService,
    private val wallApiService: WallApiService,
    private val auth: AppAuth
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val getAllPostsData: Flow<PagingData<PostItem>> = Pager(
        config = PagingConfig(pageSize = 5),
        remoteMediator = PostRemoteMediator(
            apiService, appDb, postDao, postRemoteKeyDao, auth
        ),
        pagingSourceFactory = postDao::pagingSource,
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getUserWallData(userId: Int): Flow<PagingData<PostItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = UserWallRemoteMediator(
                authorId = userId,
                wallApiService = wallApiService,
                appDb = appDb,
                postDao = postDao,
                userWallRemoteKeyDao = userWallRemoteKeyDao,
                auth = auth
            ),
            pagingSourceFactory = { postDao.pagingSourceByAuthorId(userId) }
        ).flow.map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }
    }

    private val currentUserId: Int
        get() = auth.authStateFlow.value.id.toInt()

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity(auth.authStateFlow.value.id.toInt()))
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: PostItem, upload: MediaUpload?) {
        try {
            val postWithAttachment = upload?.let {
                upload(it)
            }?.let {
                // TODO: add support for other types
                post.copy(attachment = Attachment(AttachmentType.IMAGE, it.url))
            }
            val response = apiService.save(postWithAttachment ?: post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(PostEntity.fromDto(body, currentUserId))
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Int) {
        var success = false
        try {
            postDao.markAsDeleting(id, true)
            val response = apiService.deletePost(id)
            if (response.isSuccessful) {
                success = true
                postDao.removeById(id)
            } else {
                throw ApiError(response.code(), response.message())
            }
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        } finally {
            if (!success) {
                postDao.markAsDeleting(id, false)
            }
        }
    }

    override suspend fun likePost(id: Int, likedByMe: Boolean) {
        try {
            val dbPost = postDao.getPostById(id)
            val isLikedByMe = dbPost.likedByMe ?: likedByMe

            Log.d("LikeDebug", "5. Статус в БД: $isLikedByMe. Отправляем запрос...")

            val response = if (isLikedByMe) {
                apiService.dislikeById(id)
            } else {
                apiService.likeById(id)

            }

            if (!response.isSuccessful) {
                Log.e(
                    "LikeDebug",
                    "6. Ошибка API: code=${response.code()}, message=${response.message()}"
                )
                throw ApiError(response.code(), response.message())
            }

            Log.d("LikeDebug", "7. Успешный ответ API. Обновляем БД.")

            val updatedPost = response.body()
                ?: throw ApiError(response.code(), "Пустой ответ от сервера при обновлении лайка")


            postDao.insert(
                PostEntity.fromDto(updatedPost, currentUserId)
            )

        } catch (e: ApiError) {
            //Ловим ApiError отдельно и пробрасываем его дальше во ViewModel для перенаправления на логин
            Log.e("LikeDebug", "8. Пробрасываем ApiError во ViewModel", e)
            throw e
        } catch (e: IOException) {
            Log.e("LikeDebug", "9. Сетевая ошибка (NetworkError)", e)
            throw NetworkError
        } catch (e: Exception) {
            Log.e("LikeDebug", "10. Неизвестная ошибка", e)
            throw UnknownError
        }
    }

    override suspend fun restorePost(post: PostItem) {
        // Полная перезапись поста старыми данными
        if (post.isSynced) {
            postDao.insert(PostEntity.fromDto(post, currentUserId))
        } else {
            //Для постов, которые не синхронизированы, вернем флаг и исходное состояние.
            post.syncStatus?.let {
                postDao.insert(
                    PostEntity.fromDto(post, currentUserId).copy(
                        isSynced = false,
                        syncStatus = it
                    )
                )
            }
        }

    }

    override suspend fun getPostsByUserId(userId: Int): List<PostItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getJobsByUserId(userId: Int): List<JobItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(userId: Int): UserItem {
        try {
            val response = apiService.getUser(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())

        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }
}

