package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: obtener el catalogo de episodios para filtrar personajes.
 */
class GetEpisodesUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(): List<Episode> = repository.allEpisodes()
}
