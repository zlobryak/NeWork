package ru.netology.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.dao.PostRemoteKeyDao
import ru.netology.nework.data.dao.UserWallRemoteKeyDao
import ru.netology.nework.data.db.AppDb

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()

    @Provides
    fun provideUserWallRemoteKeyDao(db: AppDb): UserWallRemoteKeyDao = db.userWallRemoteKeyDao()
}