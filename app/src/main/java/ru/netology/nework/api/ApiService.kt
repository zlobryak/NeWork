package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.data.dto.AuthResponse
import ru.netology.nework.data.dto.Media
import ru.netology.nework.data.dto.PostItem

interface ApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<PostItem>>

    @POST("posts")
    suspend fun savePost(@Body post: PostItem): PostItem

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long) : Response<Unit>
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

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<PostItem>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<PostItem>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authenticate(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<AuthResponse>

    //TODO Рефакторинг авторизации

    @Multipart
    @POST("users/registration")
    suspend fun register(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part? // nullable — аватарка опциональна
    ): Response<AuthResponse>

    //TODO Рефакторинг регистрации
}

//TODO Задать вопрос куратору: Нужна ли полная реализация всего доступного API или достаточно используемого по заданию