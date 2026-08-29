package ru.netology.nework.data.dto.job

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JobItem(
    val finish: String,
    val id: Int,
    val link: String,
    val name: String,
    val position: String,
    val start: String
) : Parcelable