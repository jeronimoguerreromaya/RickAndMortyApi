package com.example.rickandmortyapi.ui

import com.example.rickandmortyapi.domain.model.CharacterFilter
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.usecase.GetCharacterDetailUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByEpisodeUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByLocationUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersUseCase
import com.example.rickandmortyapi.domain.usecase.GetEpisodesByIdsUseCase
import com.example.rickandmortyapi.domain.usecase.GetEpisodesUseCase
import com.example.rickandmortyapi.domain.usecase.GetLocationsUseCase
import com.example.rickandmortyapi.fake.FakeCharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    private val dispatcher = UnconfinedTestDispatcher()

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
        getEpisodesByIds = GetEpisodesByIdsUseCase(repository),
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
    fun `status and species filters are forwarded to the repository`() = runTest(dispatcher) {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.characters.collect { } }
        advanceUntilIdle()

        viewModel.toggleStatus("alive")
        viewModel.toggleSpecies("Human")
        advanceUntilIdle()

        assertEquals("alive", viewModel.selectedStatus.value)
        assertEquals("Human", viewModel.selectedSpecies.value)
        assertEquals(
            CharacterFilter(name = null, status = "alive", species = "Human"),
            repository.lastFilter
        )
    }

    @Test
    fun `tapping the same status chip removes the filter`() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.toggleStatus("alive")
        viewModel.toggleStatus("alive")

        assertNull(viewModel.selectedStatus.value)
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
    fun `selecting a location resets the status and species chips`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.toggleStatus("alive")
        viewModel.toggleSpecies("Human")

        viewModel.selectLocation(Location(1, "Earth", "Planet", "Dimension C-137"))
        advanceUntilIdle()

        assertNull(viewModel.selectedStatus.value)
        assertNull(viewModel.selectedSpecies.value)
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
    fun `loadDetail exposes the character and its episode names`() = runTest(dispatcher) {
        repository.detail = FakeCharacterRepository.characterFixture(
            42,
            "Birdperson",
            episodeIds = listOf(1, 2)
        )
        repository.detailEpisodes = listOf(
            FakeCharacterRepository.episodeFixture(1, "Pilot", "S01E01"),
            FakeCharacterRepository.episodeFixture(2, "Lawnmower Dog", "S01E02")
        )
        val viewModel = viewModel()

        viewModel.loadDetail(42)
        advanceUntilIdle()

        assertEquals("Birdperson", viewModel.detail.value?.name)
        assertEquals(listOf(1, 2), repository.lastEpisodeIds)
        assertEquals(2, viewModel.detailEpisodes.value.size)
        assertEquals("Pilot", viewModel.detailEpisodes.value.first().name)
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
