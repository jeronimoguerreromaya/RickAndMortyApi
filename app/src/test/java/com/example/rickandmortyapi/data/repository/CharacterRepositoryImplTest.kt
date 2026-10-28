package com.example.rickandmortyapi.data.repository

import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.data.remote.PagedResponse
import com.example.rickandmortyapi.data.remote.PageInfo
import com.example.rickandmortyapi.fake.FakeRickAndMortyApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CharacterRepositoryImplTest {

    private val api = FakeRickAndMortyApi()
    private val repository = CharacterRepositoryImpl(api)

    @Test
    fun `charactersForLocation requests the residents in batches of twenty`() = runTest {
        api.singleLocation = api.singleLocation.copy(
            residents = (1..25).map { "https://rickandmortyapi.com/api/character/$it" }
        )

        val characters = repository.charactersForLocation(1)

        assertEquals(25, characters.size)
        assertEquals(2, api.requestedCharacterIds.size)
        assertEquals(20, api.requestedCharacterIds.first().split(",").size)
        assertEquals("Personaje 25", characters.last().name)
    }

    @Test
    fun `charactersForLocation returns an empty list when there are no residents`() = runTest {
        api.singleLocation = api.singleLocation.copy(residents = emptyList())

        val characters = repository.charactersForLocation(1)

        assertTrue(characters.isEmpty())
        assertTrue(api.requestedCharacterIds.isEmpty())
    }

    @Test
    fun `charactersForEpisode maps the episode characters`() = runTest {
        api.singleEpisode = api.singleEpisode.copy(
            characters = listOf(
                "https://rickandmortyapi.com/api/character/1",
                "https://rickandmortyapi.com/api/character/2"
            )
        )

        val characters = repository.charactersForEpisode(1)

        assertEquals(2, characters.size)
        assertEquals(listOf("1,2"), api.requestedCharacterIds)
    }

    @Test
    fun `allLocations walks through every page`() = runTest {
        api.locationPages = mapOf(
            1 to PagedResponse(
                info = PageInfo(count = 2, pages = 2, next = "https://rickandmortyapi.com/api/location?page=2", prev = null),
                results = listOf(LocationDto(1, "Earth", "Planet", "Dimension C-137", emptyList(), "url"))
            ),
            2 to PagedResponse(
                info = PageInfo(count = 2, pages = 2, next = null, prev = null),
                results = listOf(LocationDto(2, "Abadango", "Cluster", "unknown", emptyList(), "url"))
            )
        )

        val locations = repository.allLocations()

        assertEquals(listOf(1, 2), api.requestedPages)
        assertEquals(2, locations.size)
        assertEquals("Abadango", locations.last().name)
        assertEquals("Dimension C-137", locations.first().dimension)
    }

    @Test
    fun `character maps the detail to the domain model`() = runTest {
        api.singleCharacter = FakeRickAndMortyApi.characterDto(7, name = "Birdperson", species = "Bird")

        val character = repository.character(7)

        assertEquals(7, character.id)
        assertEquals("Birdperson", character.name)
        assertEquals("Bird", character.species)
    }

    @Test
    fun `failures are propagated to the caller`() = runTest {
        api.error = IOException("sin red")

        val failure = runCatching { repository.character(1) }.exceptionOrNull()

        assertTrue(failure is IOException)
    }
}
