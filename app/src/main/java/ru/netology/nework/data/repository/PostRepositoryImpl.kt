package ru.netology.nework.data.repository

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
import ru.netology.nework.data.db.AppDb
import ru.netology.nework.data.dto.Media
import ru.netology.nework.data.dto.MediaUpload
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.dao.PostRemoteKeyDao
import ru.netology.nework.data.dto.Attachment
import ru.netology.nework.data.entity.AttachmentType
import ru.netology.nework.data.entity.toEntity
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    appDb: AppDb,
    private val postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    private val apiService: ApiService,
) : PostRepository {
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<PostItem>> = Pager(
        config = PagingConfig(pageSize = 5),
        remoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource,
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toDto)
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            postDao.insert(body.toEntity())
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCount(id: Long): Flow<Int> {
        TODO("Not yet implemented")
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
            postDao.insert(PostEntity.fromDto(body))
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
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

    //TODO Тут не будет обращения к базе данных для изменения количества лайков,
    //В базе хранится список ID лайкнувших, если лайкаем свой пост - меняем статус likedByMe
    //Количество лайков вычисляется из списка Id, его менять вручную не нужно.
    //Бует ли меняться на сервере likedByMe?
    override suspend fun likeById(id: Long) {
        val isLikedByMe = postDao.getPostById(id).likedByMe

        try {

            val response = if (isLikedByMe) apiService.dislikeById(id)else apiService.likeById(id)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            } //При ошибке прерываем функцию

            postDao.likedByMe(id) //Если нет ошибок, обращаемся к базе данных, которая меняет boolean на противоположный
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

