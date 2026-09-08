package ru.netology.nework.data.dto.post

import java.io.File

data class Media(val url: String)

data class MediaUpload(val file: File)