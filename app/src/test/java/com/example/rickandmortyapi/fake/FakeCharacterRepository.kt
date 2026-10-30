package com.example.rickandmortyapi.fake

import androidx.paging.PagingData
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.CharacterFilter
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
    var detailEpisodes: List<Episode> = emptyList()
    var error: Throwable? = null

    var lastFilter: CharacterFilter? = null
    var lastLocationId: Int? = null
    var lastEpisodeId: Int? = null
    var lastEpisodeIds: List<Int> = emptyList()

    override fun characters(filter: CharacterFilter): Flow<PagingData<Character>> {
        lastFilter = filter
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

    override suspend fun episodesByIds(ids: List<Int>): List<Episode> {
        lastEpisodeIds = ids
        return detailEpisodes
    }

    companion object {
        fun characterFixture(
            id: Int,
            name: String = "Personaje $id",
            episodeIds: List<Int> = listOf(1, 2)
        ) = Character(
            id = id,
            name = name,
            status = "Alive",
            species = "Human",
            type = "",
            gender = "Male",
            originName = "Earth",
            lastKnownLocation = "Earth",
            imageUrl = "https://example.com/$id.png",
            episodeIds = episodeIds,
            createdDate = "2017-12-02"
        )

        fun episodeFixture(id: Int, name: String = "Episodio $id", code: String = "S01E%02d".format(id)) =
            Episode(id = id, name = name, airDate = "2013-12-02", code = code)
    }
}
