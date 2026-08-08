package ru.netology.nework.data.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nework.data.entity.AttachmentType

@Parcelize
data class Attachment(
    val type: AttachmentType,
    val url: String
): Parcelable