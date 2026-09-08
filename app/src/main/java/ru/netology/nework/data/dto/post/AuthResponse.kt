package ru.netology.nework.data.dto.post

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("token") val token: String,
    @SerializedName("avatar") val avatar: String?
)