package ru.netology.nework.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.data.dao.eventDao.EventDao
import ru.netology.nework.data.dao.eventDao.EventRemoteKeyDao
import ru.netology.nework.data.dao.postDao.PostDao
import ru.netology.nework.data.entity.postEntity.PostEntity
import ru.netology.nework.data.entity.postEntity.PostRemoteKeyEntity
import ru.netology.nework.data.dao.postDao.PostRemoteKeyDao
import ru.netology.nework.data.dao.postDao.UserWallRemoteKeyDao
import ru.netology.nework.data.entity.eventEntity.EventEntity
import ru.netology.nework.data.entity.eventEntity.EventRemoteKeyEntity
import ru.netology.nework.data.entity.postEntity.UserWallPostRemoteKeyEntity

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class,
        UserWallPostRemoteKeyEntity::class,
        EventEntity::class,
        EventRemoteKeyEntity::class,
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun userWallRemoteKeyDao(): UserWallRemoteKeyDao //Пагинация только для фрагмента со стеной пользователя
    abstract fun eventDao(): EventDao
    abstract fun eventRemoteKeyDao(): EventRemoteKeyDao
}
