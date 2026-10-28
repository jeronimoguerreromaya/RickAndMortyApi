package com.example.rickandmortyapi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.domain.usecase.GetCharacterDetailUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByEpisodeUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersByLocationUseCase
import com.example.rickandmortyapi.domain.usecase.GetCharactersUseCase
import com.example.rickandmortyapi.domain.usecase.GetEpisodesUseCase
import com.example.rickandmortyapi.domain.usecase.GetLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la aplicacion. Coordina los casos de uso y expone el estado
 * de la interfaz. Los estados de error estan separados por flujo (listado,
 * filtro, catalogo y detalle) para que un fallo en una zona no afecte a otra.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    private val getCharacters: GetCharactersUseCase,
    private val getCharacterDetail: GetCharacterDetailUseCase,
    private val getLocations: GetLocationsUseCase,
    private val getEpisodes: GetEpisodesUseCase,
    private val getCharactersByLocation: GetCharactersByLocationUseCase,
    private val getCharactersByEpisode: GetCharactersByEpisodeUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    val searchText: StateFlow<String> = query.asStateFlow()

    /** Listado paginado (scroll infinito) alimentado por el endpoint de personajes. */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val characters = query
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .flatMapLatest { getCharacters(it) }
        .cachedIn(viewModelScope)

    // ---------- Filtro activo (localizacion o episodio) ----------

    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter: StateFlow<String?> = _activeFilter.asStateFlow()

    private val _filteredCharacters = MutableStateFlow<List<Character>?>(null)
    val filteredCharacters: StateFlow<List<Character>?> = _filteredCharacters.asStateFlow()

    private val _filterBusy = MutableStateFlow(false)
    val filterBusy: StateFlow<Boolean> = _filterBusy.asStateFlow()

    private val _filterError = MutableStateFlow<String?>(null)
    val filterError: StateFlow<String?> = _filterError.asStateFlow()

    private var retryFilter: (() -> Unit)? = null

    // ---------- Catalogos para los dialogos de filtrado ----------

    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    private val _catalogBusy = MutableStateFlow(false)
    val catalogBusy: StateFlow<Boolean> = _catalogBusy.asStateFlow()

    private val _catalogError = MutableStateFlow<String?>(null)
    val catalogError: StateFlow<String?> = _catalogError.asStateFlow()

    private var locationsCache: List<Location>? = null
    private var episodesCache: List<Episode>? = null

    // ---------- Detalle ----------

    private val _detail = MutableStateFlow<Character?>(null)
    val detail: StateFlow<Character?> = _detail.asStateFlow()

    private val _detailBusy = MutableStateFlow(false)
    val detailBusy: StateFlow<Boolean> = _detailBusy.asStateFlow()

    private val _detailError = MutableStateFlow<String?>(null)
    val detailError: StateFlow<String?> = _detailError.asStateFlow()

    // ---------- Acciones ----------

    fun updateSearch(value: String) {
        query.value = value
        _filteredCharacters.value = null
        _activeFilter.value = null
        _filterError.value = null
        retryFilter = null
    }

    fun loadLocations() = viewModelScope.launch {
        val cached = locationsCache
        if (cached != null) {
            _locations.value = cached
            return@launch
        }
        _catalogBusy.value = true
        _catalogError.value = null
        runCatching { getLocations() }
            .onSuccess {
                locationsCache = it
                _locations.value = it
            }
            .onFailure { _catalogError.value = it.userMessage("No se pudieron cargar las localizaciones.") }
        _catalogBusy.value = false
    }

    fun loadEpisodes() = viewModelScope.launch {
        val cached = episodesCache
        if (cached != null) {
            _episodes.value = cached
            return@launch
        }
        _catalogBusy.value = true
        _catalogError.value = null
        runCatching { getEpisodes() }
            .onSuccess {
                episodesCache = it
                _episodes.value = it
            }
            .onFailure { _catalogError.value = it.userMessage("No se pudieron cargar los episodios.") }
        _catalogBusy.value = false
    }

    fun selectLocation(location: Location) {
        retryFilter = { selectLocation(location) }
        loadFiltered("Localización: ${location.name}") { getCharactersByLocation(location.id) }
    }

    fun selectEpisode(episode: Episode) {
        retryFilter = { selectEpisode(episode) }
        loadFiltered("Episodio: ${episode.code}") { getCharactersByEpisode(episode.id) }
    }

    fun retryActiveFilter() {
        retryFilter?.invoke()
    }

    private fun loadFiltered(label: String, request: suspend () -> List<Character>) = viewModelScope.launch {
        query.value = ""
        _activeFilter.value = label
        _filterBusy.value = true
        _filterError.value = null
        _filteredCharacters.value = emptyList()
        runCatching { request() }
            .onSuccess { _filteredCharacters.value = it }
            .onFailure { _filterError.value = it.userMessage("No se pudieron cargar los personajes.") }
        _filterBusy.value = false
    }

    fun clearFilter() {
        _activeFilter.value = null
        _filteredCharacters.value = null
        _filterError.value = null
        retryFilter = null
    }

    fun loadDetail(id: Int) = viewModelScope.launch {
        _detail.value = null
        _detailError.value = null
        _detailBusy.value = true
        runCatching { getCharacterDetail(id) }
            .onSuccess { _detail.value = it }
            .onFailure { _detailError.value = it.userMessage("No se pudo cargar el personaje.") }
        _detailBusy.value = false
    }

    fun clearDetail() {
        _detail.value = null
        _detailError.value = null
        _detailBusy.value = false
    }

    private fun Throwable.userMessage(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
