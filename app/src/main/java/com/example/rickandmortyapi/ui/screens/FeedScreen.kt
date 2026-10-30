package com.example.rickandmortyapi.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.CharacterFilter
import com.example.rickandmortyapi.ui.components.CharacterCard
import com.example.rickandmortyapi.ui.components.StatusMessage

/**
 * Pantalla principal: buscador por nombre, filtros por estado/especie,
 * filtros por localizacion/episodio y listado paginado de personajes.
 */
@Composable
fun FeedScreen(
    search: String,
    status: String?,
    species: String?,
    filterLabel: String?,
    filteredCharacters: List<Character>?,
    filterBusy: Boolean,
    filterError: String?,
    characters: LazyPagingItems<Character>,
    onSearch: (String) -> Unit,
    onStatus: (String) -> Unit,
    onSpecies: (String) -> Unit,
    onClearAdvanced: () -> Unit,
    onPickLocation: () -> Unit,
    onPickEpisode: () -> Unit,
    onClearFilter: () -> Unit,
    onRetryFilter: () -> Unit,
    onCharacter: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                "Rick and Morty",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            if (filterLabel == null) {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar personaje por nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(8.dp))
                FilterChipsRow(
                    title = "Estado",
                    options = CharacterFilter.STATUS_OPTIONS,
                    selected = status,
                    onToggle = onStatus
                )
                FilterChipsRow(
                    title = "Especie",
                    options = CharacterFilter.SPECIES_OPTIONS,
                    selected = species,
                    onToggle = onSpecies
                )
                if (status != null || species != null) {
                    TextButton(onClick = onClearAdvanced) { Text("Limpiar filtros") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onPickLocation, modifier = Modifier.weight(1f)) {
                        Text("Localización")
                    }
                    OutlinedButton(onClick = onPickEpisode, modifier = Modifier.weight(1f)) {
                        Text("Episodio")
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        filterLabel,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onClearFilter) { Text("Quitar filtro") }
                }
            }
        }

        if (filterLabel != null) {
            FilteredCharacterList(
                filteredCharacters = filteredCharacters,
                busy = filterBusy,
                error = filterError,
                onRetry = onRetryFilter,
                onCharacter = onCharacter
            )
        } else {
            PagedCharacterList(characters = characters, onCharacter = onCharacter)
        }
    }
}

@Composable
private fun FilterChipsRow(
    title: String,
    options: List<String>,
    selected: String?,
    onToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        options.forEach { option ->
            FilterChip(
                selected = selected.equals(option, ignoreCase = true),
                onClick = { onToggle(option) },
                label = { Text(option.replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun FilteredCharacterList(
    filteredCharacters: List<Character>?,
    busy: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onCharacter: (Character) -> Unit
) {
    when {
        busy && filteredCharacters.isNullOrEmpty() -> StatusMessage("Cargando personajes…", loading = true)
        error != null -> StatusMessage(error, action = onRetry)
        filteredCharacters.isNullOrEmpty() -> StatusMessage("No se encontraron personajes.")
        else -> LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCharacters, key = { it.id }) { character ->
                CharacterCard(character) { onCharacter(character) }
            }
        }
    }
}

@Composable
private fun PagedCharacterList(
    characters: LazyPagingItems<Character>,
    onCharacter: (Character) -> Unit
) {
    when (val state = characters.loadState.refresh) {
        is LoadState.Loading -> if (characters.itemCount == 0) {
            StatusMessage("Cargando personajes…", loading = true)
        }

        is LoadState.Error -> if (characters.itemCount == 0) {
            StatusMessage(
                state.error.localizedMessage ?: "Error al cargar.",
                action = { characters.retry() }
            )
        }

        else -> Unit
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(characters.itemCount) { index ->
            characters[index]?.let { character ->
                CharacterCard(character) { onCharacter(character) }
            }
        }
        if (characters.loadState.append is LoadState.Loading) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (characters.loadState.append is LoadState.Error) {
            item {
                StatusMessage("No se pudieron cargar más personajes.", action = { characters.retry() })
            }
        }
        if (characters.loadState.refresh is LoadState.NotLoading && characters.itemCount == 0) {
            item { StatusMessage("No se encontraron personajes.") }
        }
    }
}
