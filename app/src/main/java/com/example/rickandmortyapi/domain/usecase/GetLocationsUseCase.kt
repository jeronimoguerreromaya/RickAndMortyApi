package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener el catalogo de localizaciones para filtrar personajes.
 */
class GetLocationsUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(): List<Location> = repository.allLocations()
}
