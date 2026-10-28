package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener el detalle de un personaje concreto.
 */
class GetCharacterDetailUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(id: Int): Character = repository.character(id)
}
