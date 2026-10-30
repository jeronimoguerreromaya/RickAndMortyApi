package com.example.rickandmortyapi.domain.model

/**
 * Modelo de dominio de un personaje. Es la representacion que usa la UI,
 * independiente del formato JSON que devuelve la API.
 */
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val originName: String,
    val lastKnownLocation: String,
    val imageUrl: String,
    val episodeIds: List<Int>,
    val createdDate: String
) {
    val episodeCount: Int get() = episodeIds.size
}
