package ru.netology.nework.data.dao.eventDao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.data.entity.eventEntity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM EventEntity ORDER BY datetime DESC")
    fun pagingSource(): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM EventEntity ORDER BY datetime DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM EventEntity")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("DELETE FROM EventEntity")
    suspend fun removeAll()

    @Query("UPDATE EventEntity SET isDeleting = :deleting WHERE id = :id")
    suspend fun markAsDeleting(id: Int, deleting: Boolean)

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun getEventById(id: Int): EventEntity?

    // Очистка для RemoteMediator при обновлении данных
    @Query("DELETE FROM EventEntity")
    suspend fun clearAll()
}