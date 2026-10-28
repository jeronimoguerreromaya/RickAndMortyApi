package com.example.rickandmortyapi.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.EpisodeDto
import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.data.remote.RickAndMortyApi
import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val api: RickAndMortyApi) {
    fun characters(name: String?): Flow<PagingData<CharacterDto>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { CharacterPagingSource(api, name) }
    ).flow

    suspend fun allLocations(): List<LocationDto> = loadPages { page -> api.locations(page).let { it.results to it.info.next } }
    suspend fun allEpisodes(): List<EpisodeDto> = loadPages { page -> api.episodes(page).let { it.results to it.info.next } }

    private suspend fun <T> loadPages(request: suspend (Int) -> Pair<List<T>, String?>): List<T> {
        val all = mutableListOf<T>()
        var page = 1
        var next: String? = "start"
        while (next != null) {
            val result = request(page)
            all += result.first
            next = result.second
            page++
        }
        return all
    }

    suspend fun charactersForLocation(id: Int): List<CharacterDto> = charactersForUrls(api.location(id).residents)
    suspend fun charactersForEpisode(id: Int): List<CharacterDto> = charactersForUrls(api.episode(id).characters)
    suspend fun character(id: Int): CharacterDto = api.character(id)

    private suspend fun charactersForUrls(urls: List<String>): List<CharacterDto> {
        val ids = urls.mapNotNull { it.substringAfterLast('/').toIntOrNull() }
        return ids.chunked(20).flatMap { batch ->
            when (batch.size) {
                0 -> emptyList()
                1 -> listOf(api.character(batch.single()))
                else -> api.charactersByIds(batch.joinToString(","))
            }
        }
    }
}
