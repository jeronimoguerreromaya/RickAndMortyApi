package com.example.rickandmortyapi.domain.model

/**
 * Filtros que se envian a la API en el endpoint de personajes.
 * Todos son opcionales y se pueden combinar entre si.
 */
data class CharacterFilter(
    val name: String? = null,
    val status: String? = null,
    val species: String? = null
) {
    val isEmpty: Boolean
        get() = name.isNullOrBlank() && status == null && species == null

    companion object {
        val None = CharacterFilter()

        /** Valores admitidos por la API para el parametro status. */
        val STATUS_OPTIONS = listOf("alive", "dead", "unknown")

        /** Especies mas comunes del universo Rick and Morty. */
        val SPECIES_OPTIONS = listOf("Human", "Alien", "Humanoid", "Robot")
    }
}
