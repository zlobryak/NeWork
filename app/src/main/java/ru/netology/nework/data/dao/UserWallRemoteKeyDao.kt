package ru.netology.nework.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.data.entity.UserWallRemoteKeyEntity

@Dao
interface UserWallRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: UserWallRemoteKeyEntity)

    @Query("SELECT * FROM user_wall_remote_keys WHERE authorId = :authorId AND type = :type LIMIT 1")
    suspend fun getRemoteKeyForAuthor(
        authorId: Int,
        type: UserWallRemoteKeyEntity.KeyType = UserWallRemoteKeyEntity.KeyType.AFTER
    ): UserWallRemoteKeyEntity?

    @Query("DELETE FROM user_wall_remote_keys WHERE authorId = :authorId")
    suspend fun removeKeysForAuthor(authorId: Int)
}