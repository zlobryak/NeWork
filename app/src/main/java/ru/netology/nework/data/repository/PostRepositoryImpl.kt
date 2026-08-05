package ru.netology.nework.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        TODO("Not yet implemented")
    }

    override suspend fun removeById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun likeById(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun upload(upload: MediaUpload): Media {
        TODO("Not yet implemented")
    }
}