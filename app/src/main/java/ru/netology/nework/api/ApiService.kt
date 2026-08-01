package ru.netology.nework.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nework.data.dto.Post

interface ApiService {
    @GET("posts")
    suspend fun getAll(): List<Post>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)
}