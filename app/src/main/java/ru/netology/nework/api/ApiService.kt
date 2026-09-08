package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.post.AuthResponse
import ru.netology.nework.data.dto.post.Media
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.data.dto.user.UserItem

interface ApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<PostItem>>

    @POST("posts")
    suspend fun savePost(@Body post: PostItem): PostItem

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): Response<Unit>

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

    @POST("posts")
    suspend fun save(@Body post: PostItem): Response<PostItem>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Int): Response<PostItem>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Int): Response<PostItem>

    @POST("users/authentication")
    suspend fun authenticate(
        @Query("login") login: String,
        @Query("pass") password: String
    ): Response<AuthResponse>

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part avatar: MultipartBody.Part // Всегда не null
    ): Response<AuthResponse>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId:  Int): Response<UserItem>

}

//TODO Задать вопрос куратору: Нужна ли полная реализация всего доступного API или достаточно используемого по заданию