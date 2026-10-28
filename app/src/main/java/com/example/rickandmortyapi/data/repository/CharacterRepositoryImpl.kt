package com.example.rickandmortyapi.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rickandmortyapi.data.mapper.toDomain
import com.example.rickandmortyapi.data.mapper.toDomainCharacters
import com.example.rickandmortyapi.data.mapper.toDomainEpisodes
import com.example.rickandmortyapi.data.mapper.toDomainLocations
import com.example.rickandmortyapi.data.paging.CharacterPagingSource
import com.example.rickandmortyapi.data.remote.RickAndMortyApi
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion concreta del acceso a datos: habla con la API REST a traves
 * de Retrofit y transforma las respuestas en modelos de dominio.
 */
@Singleton
class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi
) : CharacterRepository {

    override fun characters(name: String?): Flow<PagingData<Character>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { CharacterPagingSource(api, name) }
    ).flow

    override suspend fun allLocations(): List<Location> = loadAllPages(
        request = { page -> api.locations(page).let { it.results to it.info.next } },
        transform = { it.toDomainLocations() }
    )

    override suspend fun allEpisodes(): List<Episode> = loadAllPages(
        request = { page -> api.episodes(page).let { it.results to it.info.next } },
        transform = { it.toDomainEpisodes() }
    )

    override suspend fun charactersForLocation(locationId: Int): List<Character> =
        charactersForUrls(api.location(locationId).residents)

    override suspend fun charactersForEpisode(episodeId: Int): List<Character> =
        charactersForUrls(api.episode(episodeId).characters)

    override suspend fun character(id: Int): Character = api.character(id).toDomain()

    /**
     * Recorre todas las paginas de un recurso paginado y acumula los resultados.
     */
    private suspend fun <D, T> loadAllPages(
        request: suspend (Int) -> Pair<List<D>, String?>,
        transform: (List<D>) -> List<T>
    ): List<T> {
        val all = mutableListOf<T>()
        var page = 1
        var hasNext = true
        while (hasNext) {
            val (items, next) = request(page)
            all += transform(items)
            hasNext = next != null
            page++
        }
        return all
    }

    /**
     * La API expone el detalle en URLs; se extraen los ids y se piden en lotes
     * de [IDS_BATCH_SIZE] (la API admite varios ids separados por comas).
     */
    private suspend fun charactersForUrls(urls: List<String>): List<Character> {
        val ids = urls.mapNotNull { it.substringAfterLast('/').toIntOrNull() }
        return ids.chunked(IDS_BATCH_SIZE).flatMap { batch ->
            when (batch.size) {
                0 -> emptyList()
                1 -> listOf(api.character(batch.single()).toDomain())
                else -> api.charactersByIds(batch.joinToString(",")).toDomainCharacters()
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val IDS_BATCH_SIZE = 20
    }
}
