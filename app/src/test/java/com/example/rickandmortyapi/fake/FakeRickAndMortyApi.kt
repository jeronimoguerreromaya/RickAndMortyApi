package com.example.rickandmortyapi.fake

import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.EpisodeDto
import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.data.remote.NamedReference
import com.example.rickandmortyapi.data.remote.PagedResponse
import com.example.rickandmortyapi.data.remote.PageInfo
import com.example.rickandmortyapi.data.remote.RickAndMortyApi

/**
 * Implementacion falsa de la API REST para poder probar la capa de datos
 * sin salir a la red.
 */
class FakeRickAndMortyApi : RickAndMortyApi {

    val requestedCharacterIds = mutableListOf<String>()
    val requestedPages = mutableListOf<Int>()

    var characterPages: Map<Int, PagedResponse<CharacterDto>> = emptyMap()
    var locationPages: Map<Int, PagedResponse<LocationDto>> = emptyMap()
    var episodePages: Map<Int, PagedResponse<EpisodeDto>> = emptyMap()
    var singleCharacter: CharacterDto = characterDto(1)
    var singleLocation: LocationDto = LocationDto(
        id = 1,
        name = "Earth",
        type = "Planet",
        dimension = "Dimension C-137",
        residents = emptyList(),
        url = "https://rickandmortyapi.com/api/location/1"
    )
    var singleEpisode: EpisodeDto = EpisodeDto(
        id = 1,
        name = "Pilot",
        air_date = "2013-12-02",
        episode = "S01E01",
        characters = emptyList(),
        url = "https://rickandmortyapi.com/api/episode/1"
    )
    var error: Throwable? = null

    override suspend fun characters(page: Int, name: String?): PagedResponse<CharacterDto> {
        requestedPages += page
        error?.let { throw it }
        return characterPages[page] ?: emptyPage()
    }

    override suspend fun charactersByIds(ids: String): List<CharacterDto> {
        requestedCharacterIds += ids
        error?.let { throw it }
        return ids.split(",").mapNotNull { it.trim().toIntOrNull() }.map { characterDto(it) }
    }

    override suspend fun character(id: Int): CharacterDto {
        error?.let { throw it }
        return singleCharacter
    }

    override suspend fun locations(page: Int): PagedResponse<LocationDto> {
        requestedPages += page
        error?.let { throw it }
        return locationPages[page] ?: emptyPage()
    }

    override suspend fun location(id: Int): LocationDto {
        error?.let { throw it }
        return singleLocation
    }

    override suspend fun episodes(page: Int): PagedResponse<EpisodeDto> {
        requestedPages += page
        error?.let { throw it }
        return episodePages[page] ?: emptyPage()
    }

    override suspend fun episode(id: Int): EpisodeDto {
        error?.let { throw it }
        return singleEpisode
    }

    private fun <T> emptyPage(): PagedResponse<T> = PagedResponse(
        info = PageInfo(count = 0, pages = 0, next = null, prev = null),
        results = emptyList()
    )

    companion object {
        fun characterDto(
            id: Int,
            name: String = "Personaje $id",
            status: String = "Alive",
            species: String = "Human",
            type: String = "",
            gender: String = "Male",
            origin: String = "Earth",
            location: String = "Earth",
            image: String = "https://example.com/$id.png",
            episodes: List<String> = listOf("https://rickandmortyapi.com/api/episode/1"),
            created: String = "2017-12-02T12:00:00.000Z"
        ) = CharacterDto(
            id = id,
            name = name,
            status = status,
            species = species,
            type = type,
            gender = gender,
            origin = NamedReference(origin, "https://rickandmortyapi.com/api/location/1"),
            location = NamedReference(location, "https://rickandmortyapi.com/api/location/2"),
            image = image,
            episode = episodes,
            url = "https://rickandmortyapi.com/api/character/$id",
            created = created
        )
    }
}
