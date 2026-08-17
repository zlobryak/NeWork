package ru.netology.nework.data.entity

import ru.netology.nework.data.dto.Coords

data class CoordsEmbeddable (
    var lat: Double,
    var long: Double
) {
    fun toDto() = Coords(lat, long)
    companion object{
        fun  fromDto(dto: Coords?) = dto?.let{
            CoordsEmbeddable(it.lat, it.long)
        }
    }
}

