package com.example.rickandmortyapi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.rickandmortyapi.data.CharacterRepository
import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.EpisodeDto
import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.data.remote.NetworkModule
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val repository = CharacterRepository(NetworkModule.api)
    private var retryFilter: (() -> Unit)? = null
    private val query = MutableStateFlow("")
    val searchText = query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val characters = query.debounce(350).distinctUntilChanged()
        .flatMapLatest { repository.characters(it) }
        .cachedIn(viewModelScope)

    private val _filteredCharacters = MutableStateFlow<List<CharacterDto>?>(null)
    val filteredCharacters = _filteredCharacters.asStateFlow()
    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter = _activeFilter.asStateFlow()
    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locations = _locations.asStateFlow()
    private val _episodes = MutableStateFlow<List<EpisodeDto>>(emptyList())
    val episodes = _episodes.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _detail = MutableStateFlow<CharacterDto?>(null)
    val detail = _detail.asStateFlow()

    fun updateSearch(value: String) {
        query.value = value
        _filteredCharacters.value = null
        _activeFilter.value = null
        _error.value = null
    }

    fun loadLocations() = viewModelScope.launch {
        _busy.value = true
        _error.value = null
        runCatching { repository.allLocations() }
            .onSuccess { _locations.value = it }
            .onFailure { _error.value = it.message ?: "No se pudieron cargar las localizaciones." }
        _busy.value = false
    }

    fun loadEpisodes() = viewModelScope.launch {
        _busy.value = true
        _error.value = null
        runCatching { repository.allEpisodes() }
            .onSuccess { _episodes.value = it }
            .onFailure { _error.value = it.message ?: "No se pudieron cargar los episodios." }
        _busy.value = false
    }

    fun selectLocation(location: LocationDto) {
        retryFilter = { selectLocation(location) }
        loadFiltered("Localización: ${location.name}") { repository.charactersForLocation(location.id) }
    }

    fun selectEpisode(episode: EpisodeDto) {
        retryFilter = { selectEpisode(episode) }
        loadFiltered("Episodio: ${episode.episode}") { repository.charactersForEpisode(episode.id) }
    }

    fun retryActiveFilter() { retryFilter?.invoke() }

    private fun loadFiltered(label: String, request: suspend () -> List<CharacterDto>) = viewModelScope.launch {
        query.value = ""
        _activeFilter.value = label
        _busy.value = true
        _error.value = null
        _filteredCharacters.value = emptyList()
        runCatching { request() }
            .onSuccess { _filteredCharacters.value = it }
            .onFailure { _error.value = it.message ?: "No se pudieron cargar los personajes." }
        _busy.value = false
    }

    fun clearFilter() {
        _activeFilter.value = null
        _filteredCharacters.value = null
        _error.value = null
        retryFilter = null
    }

    fun loadDetail(id: Int) = viewModelScope.launch {
        _detail.value = null
        _error.value = null
        _busy.value = true
        runCatching { repository.character(id) }
            .onSuccess { _detail.value = it }
            .onFailure { _error.value = it.message ?: "No se pudo cargar el personaje." }
        _busy.value = false
    }

    fun clearDetail() {
        _detail.value = null
        _error.value = null
    }
}
