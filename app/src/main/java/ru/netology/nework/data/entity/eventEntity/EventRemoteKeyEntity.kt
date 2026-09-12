package ru.netology.nework.data.entity.eventEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EventRemoteKeyEntity(
    @PrimaryKey val id: Int = 1,

    val nextPage: Int?,
    val endOfPaginationReached: Boolean
)