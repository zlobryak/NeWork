package ru.netology.nework.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.data.dto.Media
import ru.netology.nework.data.dto.MediaUpload
import ru.netology.nework.data.dto.PostItem

interface PostRepository {
    val data: Flow<PagingData<PostItem>>
    suspend fun getAll()
    suspend fun save(post: PostItem, upload: MediaUpload?)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun upload(upload: MediaUpload): Media
}
