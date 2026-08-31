package ru.netology.nework.data.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.data.dto.post.Media
import ru.netology.nework.data.dto.post.MediaUpload
import ru.netology.nework.data.dto.post.PostItem

interface PostRepository {
    val getAllPostsData: Flow<PagingData<PostItem>>

    fun getUserWallData(userId: Int): Flow<PagingData<PostItem>>
    suspend fun getAll()
    suspend fun save(post: PostItem, upload: MediaUpload?)
    suspend fun removeById(id: Int)
    suspend fun likePost(id: Int, likedByMe: Boolean)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun restorePost(post: PostItem)
    suspend fun getPostsByUserId(userId: Int): List<PostItem>
    suspend fun getJobsByUserId(userId: Int): List<JobItem>}
