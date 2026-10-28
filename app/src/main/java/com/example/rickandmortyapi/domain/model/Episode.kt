package com.example.rickandmortyapi.domain.model

/**
 * Modelo de dominio de un episodio.
 */
data class Episode(
    val id: Int,
    val name: String,
    val airDate: String,
    val code: String
)
