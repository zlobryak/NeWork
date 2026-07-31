package ru.netology.nework.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(
    val type: String,
    val url: String
): Parcelable