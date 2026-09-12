package ru.netology.nework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.data.dao.eventDao.EventDao
import ru.netology.nework.data.dao.eventDao.EventRemoteKeyDao
import ru.netology.nework.data.dao.postDao.PostDao
import ru.netology.nework.data.dao.postDao.PostRemoteKeyDao
import ru.netology.nework.data.dao.postDao.UserWallRemoteKeyDao
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

    @Provides
    fun provideUEventDao(db: AppDb): EventDao = db.eventDao()

    @Provides
    fun provideEventRemotKeyDao(db: AppDb): EventRemoteKeyDao = db.eventRemoteKeyDao()
}