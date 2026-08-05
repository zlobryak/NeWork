package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.PostItem

interface ApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<PostItem>>

    @POST("posts")
    suspend fun savePost(@Body post: PostItem): PostItem

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)
    @GET("posts/latest")
    suspend fun getLatest(@Query("count") count: Int): Response<List<PostItem>>
    @GET("posts/{id}/before")
    suspend fun getBefore(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<PostItem>>

    @GET("posts/{id}/after")
    suspend fun getAfter(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<PostItem>>
}

//TODO Задать вопрос куратору: Нужна ли полная реализация всего доступного API или достаточно используемого по заданию