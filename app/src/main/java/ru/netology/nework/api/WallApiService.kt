package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.post.PostItem

interface WallApiService {
    @GET("{authorId}/wall/")
    suspend fun getAll(@Path("authorId") authorId: Int): Response<List<PostItem>>

    @GET("{authorId}/wall/latest")
    suspend fun getLatest(
        @Path("authorId") authorId: Int,
        @Query("count") count: Int
    ): Response<List<PostItem>>

    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<PostItem>>

    @GET("posts/{authorId}/after")
    suspend fun getAfter(
        @Path("authorId") authorId: Long,
        @Query("count") count: Int
    ): Response<List<PostItem>>

}


