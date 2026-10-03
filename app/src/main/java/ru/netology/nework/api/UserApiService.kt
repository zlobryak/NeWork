package ru.netology.nework.api


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.dto.user.Users

interface UserApiService {
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int): Response<UserItem>

    /**
     * Полный список всех пользователей одним запросом.
     * Сервер не поддерживает разбиение на пачки (count/from игнорируются),
     * поэтому получаем сразу весь массив и сохраняем его в БД:
     * GET http://94.228.125.136:8080/api/users
     */
    @GET("users")
    suspend fun getAllUsers(): Response<Users>
}