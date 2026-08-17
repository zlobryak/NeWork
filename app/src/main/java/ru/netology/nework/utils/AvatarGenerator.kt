package ru.netology.nework.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.content.res.Resources
import kotlin.math.abs
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.createBitmap

object AvatarGenerator {

    private val colors = intArrayOf(
        0xFFE57373.toInt(), // red
        0xFFBA68C8.toInt(), // purple
        0xFF7986CB.toInt(), // indigo
        0xFF64B5F6.toInt(), // blue
        0xFF4FC3F7.toInt(), // light blue
        0xFF4DB6AC.toInt(), // teal
        0xFF81C784.toInt(), // green
        0xFFAED581.toInt(), // light green
        0xFFFFB74D.toInt(), // orange
        0xFFFF8A65.toInt(), // deep orange
        0xFFA1887F.toInt(), // brown
        0xFF90A4AE.toInt()  // blue grey
    )

    /**
     * Создаёт Drawable-заглушку: цветной круг с первой буквой имени.
     * @param name Имя пользователя (никнейм).
     * @param sizePx Размер картинки в пикселях (обычно равен размеру ImageView).
     * @param resources Ресурсы приложения (нужны для BitmapDrawable).
     */
    fun generate(name: String?, sizePx: Int, resources: Resources): BitmapDrawable {
        val initial = extractInitial(name)
        val backgroundColor = pickColor(name)

        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        // 1. Рисуем круг-фон
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }
        val radius = sizePx / 2f
        canvas.drawCircle(radius, radius, radius, bgPaint)

        // 2. Рисуем букву по центру
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.5f // Буква занимает ~50% высоты круга
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        // Хитрость для точного центрирования по вертикали
        val textBounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, textBounds)
        val x = sizePx / 2f
        val y = sizePx / 2f - textBounds.exactCenterY()

        canvas.drawText(initial, x, y, textPaint)

        return bitmap.toDrawable(resources)
    }

    /**
     * Безопасно извлекает первую букву.
     * Использует codePointAt, чтобы корректно работать с эмодзи и экзотическими символами.
     */
    private fun extractInitial(name: String?): String {
        if (name.isNullOrBlank()) return "?"

        val trimmed = name.trim()
        // Берём первый code point (это безопасно для суррогатных пар)
        val firstCodePoint = Character.codePointAt(trimmed, 0)
        val firstChar = Character.toString(firstCodePoint)

        // Если первая "буква" не является буквой/цифрой (например, эмодзи или символ),
        // возвращаем её как есть. Если хочешь строго буквы — можно добавить проверку.
        return firstChar.uppercase()
    }

    /**
     * Детерминированный выбор цвета по хэшу имени.
     * Один и тот же ник всегда даст один и тот же цвет.
     */
    private fun pickColor(name: String?): Int {
        val hash = abs(name?.hashCode() ?: 0)
        return colors[hash % colors.size]
    }
}