package ru.netology.nework.data.api

import retrofit2.Response
import retrofit2.http.*
import ru.netology.nework.data.dto.event.EventItem

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
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333
     */
    @PUT("events/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body event: EventItem
    ): Response<EventItem>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events333
     */
    @DELETE("events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): Response<Unit>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333/likes
     */
    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Int): Response<EventItem>

    /**
     * Формирует URL вида: http://94.228.125.136:8080/api/events/333/likes
     */
    @DELETE("events/{id}/likes")
    suspend fun dislikeEvent(@Path("id") id: Int): Response<EventItem>
}