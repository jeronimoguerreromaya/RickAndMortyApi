package com.example.rickandmortyapi.domain.usecase

import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * Caso de uso: recuperar los episodios de un personaje a partir de sus ids,
 * para enriquecer la pantalla de detalle.
 */
class GetEpisodesByIdsUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(ids: List<Int>): List<Episode> = repository.episodesByIds(ids)
}
