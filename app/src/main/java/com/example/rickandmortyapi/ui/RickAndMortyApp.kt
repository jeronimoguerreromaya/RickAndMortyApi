package com.example.rickandmortyapi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import com.example.rickandmortyapi.ui.components.ConnectionBanner
import com.example.rickandmortyapi.ui.components.PickerDialog
import com.example.rickandmortyapi.ui.components.PickerRow
import com.example.rickandmortyapi.ui.screens.DetailScreen
import com.example.rickandmortyapi.ui.screens.FeedScreen

private enum class PickerKind { LOCATION, EPISODE }

/**
 * Composable raiz de la aplicacion: gestiona la navegacion entre la lista y
 * el detalle, los dialogos de filtrado y el indicador de conexion.
 */
@Composable
fun RickAndMortyApp(viewModel: AppViewModel = hiltViewModel()) {
    val search by viewModel.searchText.collectAsStateWithLifecycle()
    val status by viewModel.selectedStatus.collectAsStateWithLifecycle()
    val species by viewModel.selectedSpecies.collectAsStateWithLifecycle()
    val filterLabel by viewModel.activeFilter.collectAsStateWithLifecycle()
    val filtered by viewModel.filteredCharacters.collectAsStateWithLifecycle()
    val filterBusy by viewModel.filterBusy.collectAsStateWithLifecycle()
    val filterError by viewModel.filterError.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val episodes by viewModel.episodes.collectAsStateWithLifecycle()
    val catalogBusy by viewModel.catalogBusy.collectAsStateWithLifecycle()
    val catalogError by viewModel.catalogError.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val detailEpisodes by viewModel.detailEpisodes.collectAsStateWithLifecycle()
    val detailBusy by viewModel.detailBusy.collectAsStateWithLifecycle()
    val detailError by viewModel.detailError.collectAsStateWithLifecycle()
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val isOnline = rememberIsOnline()

    var picker by rememberSaveable { mutableStateOf<PickerKind?>(null) }
    var detailId by rememberSaveable { mutableStateOf<Int?>(null) }

    BackHandler(enabled = detailId != null) {
        detailId = null
        viewModel.clearDetail()
    }
    LaunchedEffect(detailId) { detailId?.let(viewModel::loadDetail) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ConnectionBanner(isOnline)
            if (detailId != null) {
                DetailScreen(
                    detail = detail,
                    episodes = detailEpisodes,
                    loading = detailBusy,
                    error = detailError,
                    onRetry = { detailId?.let(viewModel::loadDetail) },
                    onBack = {
                        detailId = null
                        viewModel.clearDetail()
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                FeedScreen(
                    search = search,
                    status = status,
                    species = species,
                    filterLabel = filterLabel,
                    filteredCharacters = filtered,
                    filterBusy = filterBusy,
                    filterError = filterError,
                    characters = characters,
                    onSearch = viewModel::updateSearch,
                    onStatus = viewModel::toggleStatus,
                    onSpecies = viewModel::toggleSpecies,
                    onClearAdvanced = viewModel::clearAdvancedFilters,
                    onPickLocation = {
                        picker = PickerKind.LOCATION
                        viewModel.loadLocations()
                    },
                    onPickEpisode = {
                        picker = PickerKind.EPISODE
                        viewModel.loadEpisodes()
                    },
                    onClearFilter = viewModel::clearFilter,
                    onRetryFilter = viewModel::retryActiveFilter,
                    onCharacter = { detailId = it.id },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    when (picker) {
        PickerKind.LOCATION -> PickerDialog(
            title = "Elige una localización",
            loading = catalogBusy,
            error = catalogError,
            onDismiss = { picker = null }
        ) {
            locations.forEach { location: Location ->
                PickerRow("${location.name}\n${location.type} · ${location.dimension}") {
                    picker = null
                    viewModel.selectLocation(location)
                }
            }
        }

        PickerKind.EPISODE -> PickerDialog(
            title = "Elige un episodio",
            loading = catalogBusy,
            error = catalogError,
            onDismiss = { picker = null }
        ) {
            episodes.forEach { episode: Episode ->
                PickerRow("${episode.name}\n${episode.airDate} · ${episode.code}") {
                    picker = null
                    viewModel.selectEpisode(episode)
                }
            }
        }

        null -> Unit
    }
}
