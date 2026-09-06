package ru.netology.nework.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import ru.netology.nework.data.dto.job.JobItem

interface JobsApiService {
    /**
     * Метод для получения списка работ пользователя.
     * Формирует URL вида: http://.../api/333/wall/latest?count=10
     */
    @GET("{userId}/wall/latest")
    suspend fun getJobs(
        @Path("userId") userId: Int,
    ): Response<List<JobItem>>

    @DELETE("my/jobs/{userId}")
    fun removeJobById(userId: Any): Response<Unit>
}