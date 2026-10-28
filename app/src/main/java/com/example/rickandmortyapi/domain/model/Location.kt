package com.example.rickandmortyapi.domain.model

/**
 * Modelo de dominio de una localizacion (planeta/dimension) del universo.
 */
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String
)
