package ru.netology.nework.data.repository.jobs

import ru.netology.nework.data.dto.job.JobItem

interface JobRepository {
    suspend fun getJobs(userId: Int): List<JobItem>
    suspend fun removeJobById(id: Int)
}