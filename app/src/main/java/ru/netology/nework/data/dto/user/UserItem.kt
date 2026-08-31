package ru.netology.nework.data.dto.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserItem(
    val avatar: String? = "",
    val id: Int = 0,       // Этого поля нет в списке лайкнувших
    val login: String = "", // Этого поля нет в списке лайкнувших
    val name: String = ""
) : Parcelable