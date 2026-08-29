package ru.netology.nework.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.data.dto.user.Users

class Converters {

    private val gson = Gson()

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
    fun fromUsers(value: Users?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toUsers(value: String?): Users? {
        if (value.isNullOrEmpty()) {
            return null
        }

        val type = object : TypeToken<Users>() {}.type
        return gson.fromJson(value, type)
    }
}