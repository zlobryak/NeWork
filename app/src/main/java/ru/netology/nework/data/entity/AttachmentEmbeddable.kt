package ru.netology.nework.data.entity

import ru.netology.nework.data.dto.Attachment

data class AttachmentEmbeddable(
    var url: String = "",
    var type: AttachmentType = AttachmentType.EMPTY,
) {
    fun toDto() = Attachment(url = url, type = type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(
                it.url ?: "",
                it.type ?: AttachmentType.EMPTY
            )
        }
    }
}

enum class AttachmentType {
    IMAGE,
    VIDEO,
    EMPTY //Для случаев, когда приходит null, но все равно нужно создать базу данных
}


