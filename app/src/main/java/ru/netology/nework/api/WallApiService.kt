package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.post.PostItem

interface WallApiService {

    /**
     * Метод для получения самых свежих постов (используется при первом запуске).
     * Формирует URL вида: http://.../api/333/wall/latest?count=10
     */
    @GET("{authorId}/wall/latest")
    suspend fun getLatestWallPosts(
        @Path("authorId") authorId: Int,
        @Query("count") count: Int = 10
    ): Response<List<PostItem>>

    /**
     * Основной метод для получения постов стены с поддержкой пагинации.
     * Формирует URL вида: http://94.228.125.136:8080/api/333/wall/1278/before?count=5
     */
    @GET("{authorId}/wall/{id}/before")
    suspend fun getBefore(
        @Path("authorId") authorId: Int,
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<PostItem>>

    /**
     * Основной метод для получения постов стены с поддержкой пагинации.
     * Формирует URL вида: http://94.228.125.136:8080/api/333/wall/1278/after?count=5
     */
    @GET("{authorId}/wall/{id}/after")
    suspend fun getAfter(
        @Path("authorId") authorId: Int,
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<PostItem>>
}