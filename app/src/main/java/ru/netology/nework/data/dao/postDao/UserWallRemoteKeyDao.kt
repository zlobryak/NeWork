package ru.netology.nework.data.dao.postDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.data.entity.postEntity.UserWallPostRemoteKeyEntity

@Dao
interface UserWallRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: UserWallPostRemoteKeyEntity)

    @Query("SELECT * FROM user_wall_remote_keys WHERE authorId = :authorId AND type = :type LIMIT 1")
    suspend fun getRemoteKeyForAuthor(
        authorId: Int,
        type: UserWallPostRemoteKeyEntity.KeyType = UserWallPostRemoteKeyEntity.KeyType.AFTER
    ): UserWallPostRemoteKeyEntity?

    @Query("DELETE FROM user_wall_remote_keys WHERE authorId = :authorId")
    suspend fun removeKeysForAuthor(authorId: Int)
}