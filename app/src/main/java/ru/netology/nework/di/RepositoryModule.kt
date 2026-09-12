package ru.netology.nework.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.data.repository.events.EventRepository
import ru.netology.nework.data.repository.events.EventRepositoryImpl
import ru.netology.nework.data.repository.jobs.JobRepository
import ru.netology.nework.data.repository.jobs.JobRepositoryImpl
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.data.repository.post.PostRepositoryImpl
import ru.netology.nework.data.repository.user.UserRepository
import ru.netology.nework.data.repository.user.UserRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindJobRepository(impl: JobRepositoryImpl): JobRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
}