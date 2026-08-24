package ru.netology.nework.data.entity

import ru.netology.nework.data.dto.Attachment

data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url = url, type = type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO
}

