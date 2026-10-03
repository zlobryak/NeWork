package ru.netology.nework.data.dao.userDao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.data.entity.userEntity.UserEntity

@Dao
interface UserDao {

    /**
     * Локальный источник страниц для Pager'а.
     * Сортировка по login — список стабильный и предсказуемый для пользователя.
     */
    @Query("SELECT * FROM UserEntity ORDER BY login ASC")
    fun pagingSource(): PagingSource<Int, UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("DELETE FROM UserEntity")
    suspend fun removeAll()

    @Query("SELECT COUNT(*) FROM UserEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM UserEntity WHERE id = :userId")
    suspend fun getById(userId: Int): UserEntity?
}
