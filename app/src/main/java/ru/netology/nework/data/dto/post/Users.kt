package ru.netology.nework.data.dto.post

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Users(
    val additionalProp1: AdditionalProp1?,
    val additionalProp2: AdditionalProp1?,
    val additionalProp3: AdditionalProp1?
): Parcelable