package ru.netology.nework.data.dao.eventDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.data.entity.eventEntity.EventRemoteKeyEntity

@Dao
interface EventRemoteKeyDao {
    @Query("SELECT * FROM EventRemoteKeyEntity WHERE eventId = :id")
    suspend fun getRemoteKeyByEventId(id: Int): EventRemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKey: EventRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remoteKeys: List<EventRemoteKeyEntity>)

    @Query("DELETE FROM EventRemoteKeyEntity")
    suspend fun clearRemoteKeys()
}