package ru.netology.nework.data.dto.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem(
    val avatar: String,
    val id: Int,
    val login: String,
    val name: String
) : Parcelable