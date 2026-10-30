package com.example.rickandmortyapi.domain.usecase

import androidx.paging.PagingData
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.CharacterFilter
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso: obtener el listado paginado de personajes aplicando
 * los filtros de nombre, estado y especie.
 */
class GetCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    operator fun invoke(filter: CharacterFilter): Flow<PagingData<Character>> =
        repository.characters(filter)
}
