package ru.netology.nework.data.repository.jobs

import ru.netology.nework.api.JobsApiService
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val jobsApiService: JobsApiService
) : JobRepository {

    override suspend fun getJobs(userId: Int): List<JobItem> {
        try {
            val response = jobsApiService.getJobs(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: emptyList()
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeJobById(id: Int) {
        try {
            val response = jobsApiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }
}