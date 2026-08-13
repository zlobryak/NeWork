package ru.netology.nework.view

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nework.R

fun ImageView.load(url: String?, vararg transforms: BitmapTransformation = emptyArray()) =
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.avatar)
        .error(R.drawable.avatar)
        .timeout(10_000)
        .transform(*transforms)
        .into(this)

fun ImageView.loadCircleCrop(url: String?, vararg transforms: BitmapTransformation = emptyArray()) =
    load(url, CircleCrop(), *transforms)
