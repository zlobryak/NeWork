package ru.netology.nework.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.data.dto.user.UserItem

class Converters {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, UserItem>>() {}.type

    @TypeConverter
    fun fromIntList(value: List<Int>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value.isNullOrEmpty()) {
            return null
        }

        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromUsersMap(map: Map<String, UserItem>?): String? {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toUsersMap(json: String?): Map<String, UserItem>? {
        if (json == null) return null
        return gson.fromJson(json, mapType)
    }
}