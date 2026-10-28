package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener los personajes que residen en una localizacion.
 */
class GetCharactersByLocationUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(locationId: Int): List<Character> =
        repository.charactersForLocation(locationId)
}
