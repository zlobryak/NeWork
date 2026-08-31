package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.post.PostItem

interface WallApiService {

    /**
     * Основной метод для получения постов стены с поддержкой пагинации.
     * Формирует URL вида: http://.../api/333/wall?key=123&count=10
     */
    @GET("{authorId}/wall")
    suspend fun getWallPosts(
        @Path("authorId") authorId: Int,
        @Query("key") key: Int? = null,  // ID последнего поста для загрузки следующей страницы
        @Query("count") count: Int = 10  // Размер страницы (должен совпадать с pageSize в PagingConfig)
    ): Response<List<PostItem>>

    /**
     * Метод для получения самых свежих постов (используется при первом запуске или Pull-to-Refresh).
     * Формирует URL вида: http://.../api/333/wall/latest?count=10
     */
    @GET("{authorId}/wall/latest")
    suspend fun getLatestWallPosts(
        @Path("authorId") authorId: Int,
        @Query("count") count: Int = 10
    ): Response<List<PostItem>>
}