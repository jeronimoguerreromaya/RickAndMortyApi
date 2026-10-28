package com.example.rickandmortyapi.fake

import androidx.paging.PagingData
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Falso del repositorio de dominio para probar el ViewModel sin red.
 */
class FakeCharacterRepository : CharacterRepository {

    var locations: List<Location> = emptyList()
    var episodes: List<Episode> = emptyList()
    var locationCharacters: List<Character> = emptyList()
    var episodeCharacters: List<Character> = emptyList()
    var detail: Character = characterFixture(1)
    var error: Throwable? = null

    var lastSearchName: String? = null
    var lastLocationId: Int? = null
    var lastEpisodeId: Int? = null

    override fun characters(name: String?): Flow<PagingData<Character>> {
        lastSearchName = name
        return flowOf(PagingData.from(emptyList()))
    }

    override suspend fun allLocations(): List<Location> {
        error?.let { throw it }
        return locations
    }

    override suspend fun allEpisodes(): List<Episode> {
        error?.let { throw it }
        return episodes
    }

    override suspend fun charactersForLocation(locationId: Int): List<Character> {
        error?.let { throw it }
        lastLocationId = locationId
        return locationCharacters
    }

    override suspend fun charactersForEpisode(episodeId: Int): List<Character> {
        error?.let { throw it }
        lastEpisodeId = episodeId
        return episodeCharacters
    }

    override suspend fun character(id: Int): Character {
        error?.let { throw it }
        return detail
    }

    companion object {
        fun characterFixture(id: Int, name: String = "Personaje $id") = Character(
            id = id,
            name = name,
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            originName = "Earth",
            lastKnownLocation = "Earth",
            imageUrl = "https://example.com/$id.png",
            episodeCount = 2,
            createdDate = "2017-12-02"
        )
    }
}
