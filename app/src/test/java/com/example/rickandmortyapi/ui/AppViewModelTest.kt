package com.example.rickandmortyapi.ui

import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.usecase.GetCharacterDetailUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByEpisodeUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByLocationUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersUseCase
import com.example.rickandmortyapi.domain.usecase.GetEpisodesUseCase
import com.example.rickandmortyapi.domain.usecase.GetLocationsUseCase
import com.example.rickandmortyapi.fake.FakeCharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val repository = FakeCharacterRepository()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AppViewModel(
        getCharacters = GetCharactersUseCase(repository),
        getCharacterDetail = GetCharacterDetailUseCase(repository),
        getLocations = GetLocationsUseCase(repository),
        getEpisodes = GetEpisodesUseCase(repository),
        getCharactersByLocation = GetCharactersByLocationUseCase(repository),
        getCharactersByEpisode = GetCharactersByEpisodeUseCase(repository)
    )

    @Test
    fun `updateSearch updates the query and resets the active filter`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.updateSearch("rick")

        assertEquals("rick", viewModel.searchText.value)
        assertNull(viewModel.activeFilter.value)
    }

    @Test
    fun `selectLocation loads the characters of that location`() = runTest(dispatcher) {
        repository.locationCharacters = listOf(FakeCharacterRepository.characterFixture(1, "Rick Sanchez"))
        val viewModel = viewModel()

        viewModel.selectLocation(Location(1, "Earth", "Planet", "Dimension C-137"))
        advanceUntilIdle()

        assertEquals("Localización: Earth", viewModel.activeFilter.value)
        assertEquals(1, repository.lastLocationId)
        assertEquals("Rick Sanchez", viewModel.filteredCharacters.value?.first()?.name)
        assertNull(viewModel.filterError.value)
    }

    @Test
    fun `selectEpisode loads the characters of that episode`() = runTest(dispatcher) {
        repository.episodeCharacters = listOf(FakeCharacterRepository.characterFixture(2, "Morty"))
        val viewModel = viewModel()

        viewModel.selectEpisode(Episode(1, "Pilot", "2013-12-02", "S01E01"))
        advanceUntilIdle()

        assertEquals("Episodio: S01E01", viewModel.activeFilter.value)
        assertEquals(1, repository.lastEpisodeId)
        assertEquals("Morty", viewModel.filteredCharacters.value?.first()?.name)
    }

    @Test
    fun `a filter error does not affect the detail error`() = runTest(dispatcher) {
        repository.error = IOException("sin red")
        val viewModel = viewModel()

        viewModel.selectLocation(Location(1, "Earth", "Planet", "Dimension C-137"))
        advanceUntilIdle()

        assertEquals("sin red", viewModel.filterError.value)
        assertNull(viewModel.detailError.value)
    }

    @Test
    fun `loadDetail exposes the character of the selected id`() = runTest(dispatcher) {
        repository.detail = FakeCharacterRepository.characterFixture(42, "Birdperson")
        val viewModel = viewModel()

        viewModel.loadDetail(42)
        advanceUntilIdle()

        assertEquals("Birdperson", viewModel.detail.value?.name)
        assertNull(viewModel.detailError.value)
    }

    @Test
    fun `clearFilter restores the paged list`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.selectLocation(Location(1, "Earth", "Planet", "Dimension C-137"))
        advanceUntilIdle()

        viewModel.clearFilter()

        assertNull(viewModel.activeFilter.value)
        assertNull(viewModel.filteredCharacters.value)
    }

    @Test
    fun `catalogs are loaded once and reused`() = runTest(dispatcher) {
        repository.locations = listOf(Location(1, "Earth", "Planet", "Dimension C-137"))
        val viewModel = viewModel()

        viewModel.loadLocations()
        advanceUntilIdle()
        viewModel.loadLocations()
        advanceUntilIdle()

        assertEquals(1, viewModel.locations.value.size)
        assertEquals("Earth", viewModel.locations.value.first().name)
    }
}
