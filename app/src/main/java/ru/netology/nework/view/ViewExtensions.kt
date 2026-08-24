package ru.netology.nework.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nework.R
import ru.netology.nework.utils.AvatarGenerator

fun ImageView.loadAvatar(url: String?, authorName: String?, vararg transforms: BitmapTransformation = emptyArray()) {
    if (url.isNullOrEmpty()) {
        Glide.with(this).clear(this)
        // Размер берём из самого ImageView (он уже должен иметь заданные размеры из XML)
        val size = if (width > 0) width else resources.getDimensionPixelSize(R.dimen.avatar_size)
        val fallback = AvatarGenerator.generate(authorName, size, resources)
        setImageDrawable(fallback)
        return
    }

    Glide.with(this)
        .load(url)
        .placeholder(AvatarGenerator.generate(authorName, width.coerceAtLeast(1), resources))
        .error(AvatarGenerator.generate(authorName, width.coerceAtLeast(1), resources))
        .transform(CircleCrop(), *transforms)
        .into(this)
}

fun ImageView.loadAttachment(url: String?) {
    Glide.with(context)
        .load(url)
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}
