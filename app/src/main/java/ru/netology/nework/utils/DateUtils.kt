package ru.netology.nework.utils

import android.util.Log
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    // Формат, который требуется по заданию: 21.08.2026 17:41
    private var outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun formatIsoDate(isoDateString: String): String {
        return try {
            val instant = Instant.parse(isoDateString)
            // Конвертируем UTC в локальный часовой пояс устройства и форматируем
            instant.atZone(ZoneId.systemDefault()).format(outputFormatter)
        } catch (e: Exception) {
            // Если по какой-то причине строка невалидна, вернем её как есть
             Log.e("DateUtils", "Ошибка парсинга даты: $isoDateString", e)
            isoDateString
        }
    }
}