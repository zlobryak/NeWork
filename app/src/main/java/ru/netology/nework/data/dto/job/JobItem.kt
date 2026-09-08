package ru.netology.nework.data.dto.job

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JobItem(
    val id: Int,
    val link: String,
    val name: String,
    val position: String,
    val start: String,
    val finish: String,
) : Parcelable