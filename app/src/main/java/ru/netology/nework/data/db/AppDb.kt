package ru.netology.nework.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.data.dao.PostDao
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.data.entity.PostRemoteKeyEntity
import ru.netology.nework.data.dao.PostRemoteKeyDao

@Database(entities = [PostEntity::class, PostRemoteKeyEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}