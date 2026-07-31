package ru.netology.nework.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coords(
    val lat: Int,
    val long: Int
): Parcelable