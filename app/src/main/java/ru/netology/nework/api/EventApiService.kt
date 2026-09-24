package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.post.Media

interface EventApiService {
    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/latest?count=15
     */
    @GET("events")
    suspend fun getEvents(
        @Query("count") count: Int,
        @Query("before") before: Int? = null
    ): Response<List<EventItem>>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events
     */
    @POST("events")
    suspend fun createEvent(@Body event: EventItem): Response<EventItem>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/media
     * Тот же эндпоинт загрузки изображений, что и для постов
     */
    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>


    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333
     */
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): Response<Unit>


    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Int): Response<EventItem>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333/likes
     */
    @DELETE("events/{id}/likes")
    suspend fun dislikeEvent(@Path("id") id: Int): Response<EventItem>


    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333/participants
     */
    @POST("events/{id}/participants")
    suspend fun participateEvent(@Path("id") id: Int): Response<EventItem>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333/participants
     */
    @DELETE("events/{id}/participants")
    suspend fun disparticipateEvent(@Path("id") id: Int): Response<EventItem>

    //TODO Добавить в фрагмент и вьюмодель участие/удаление из участников

}