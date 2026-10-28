package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener los personajes que aparecen en un episodio.
 */
class GetCharactersByEpisodeUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(episodeId: Int): List<Character> =
        repository.charactersForEpisode(episodeId)
}
