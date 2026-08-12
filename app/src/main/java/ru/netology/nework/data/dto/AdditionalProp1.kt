package ru.netology.nework.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdditionalProp1(
    val avatar: String?,
    val name: String?
): Parcelable