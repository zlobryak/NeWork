package ru.netology.nework.data.dao.eventDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.data.entity.eventEntity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {

    // Получаем нашу единственную строку с глобальным ключом
    @Query("SELECT * FROM EventRemoteKeyEntity LIMIT 1")
    suspend fun getRemoteKey(): EventRemoteKeyEntity?

    // REPLACE работает корректно только если у Entity есть PrimaryKey
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(key: EventRemoteKeyEntity)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clear()
}