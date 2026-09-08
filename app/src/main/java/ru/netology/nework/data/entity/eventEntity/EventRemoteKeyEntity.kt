package ru.netology.nework.data.entity.eventEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EventRemoteKeyEntity")
data class EventRemoteKeyEntity(
    @PrimaryKey val eventId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)