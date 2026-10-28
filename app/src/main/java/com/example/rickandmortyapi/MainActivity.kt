package com.example.rickandmortyapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.EpisodeDto
import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.ui.AppViewModel
import com.example.rickandmortyapi.ui.rememberIsOnline
import com.example.rickandmortyapi.ui.theme.RickAndMortyApiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RickAndMortyApiTheme { RickAndMortyApp() } }
    }
}

@Composable
private fun RickAndMortyApp(viewModel: AppViewModel = viewModel()) {
    val search by viewModel.searchText.collectAsStateWithLifecycle()
    val filter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val filtered by viewModel.filteredCharacters.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val characters = viewModel.characters.collectAsLazyPagingItems()
    val isOnline = rememberIsOnline()
    var dialog by remember { mutableStateOf<PickerType?>(null) }
    var detailId by remember { mutableStateOf<Int?>(null) }

    BackHandler(enabled = detailId != null) {
        detailId = null
        viewModel.clearDetail()
    }
    LaunchedEffect(detailId) { detailId?.let(viewModel::loadDetail) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        if (detailId != null) {
            DetailScreen(
                detail = detail,
                loading = busy,
                error = error,
                onRetry = { detailId?.let(viewModel::loadDetail) },
                onBack = { detailId = null; viewModel.clearDetail() },
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (!isOnline) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                        Text("Sin conexión a internet", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                FeedScreen(
                search = search,
                filter = filter,
                filtered = filtered,
                busy = busy,
                error = error,
                characters = characters,
                onSearch = viewModel::updateSearch,
                onPickLocation = { dialog = PickerType.LOCATION; viewModel.loadLocations() },
                onPickEpisode = { dialog = PickerType.EPISODE; viewModel.loadEpisodes() },
                onClearFilter = viewModel::clearFilter,
                onRetryFilter = viewModel::retryActiveFilter,
                onCharacter = { detailId = it.id },
                modifier = Modifier.weight(1f)
                )
            }
        }
    }

    when (dialog) {
        PickerType.LOCATION -> {
            val locations by viewModel.locations.collectAsStateWithLifecycle()
            PickerDialog("Elige una localización", busy, error, onDismiss = { dialog = null }) {
                locations.forEach { location ->
                    PickerRow("${location.name}\n${location.type} · ${location.dimension}") {
                        dialog = null
                        viewModel.selectLocation(location)
                    }
                }
            }
        }
        PickerType.EPISODE -> {
            val episodes by viewModel.episodes.collectAsStateWithLifecycle()
            PickerDialog("Elige un episodio", busy, error, onDismiss = { dialog = null }) {
                episodes.forEach { episode ->
                    PickerRow("${episode.name}\n${episode.air_date} · ${episode.episode}") {
                        dialog = null
                        viewModel.selectEpisode(episode)
                    }
                }
            }
        }
        null -> Unit
    }
}

private enum class PickerType { LOCATION, EPISODE }

@Composable
private fun FeedScreen(
    search: String,
    filter: String?,
    filtered: List<CharacterDto>?,
    busy: Boolean,
    error: String?,
    characters: androidx.paging.compose.LazyPagingItems<CharacterDto>,
    onSearch: (String) -> Unit,
    onPickLocation: () -> Unit,
    onPickEpisode: () -> Unit,
    onClearFilter: () -> Unit,
    onRetryFilter: () -> Unit,
    onCharacter: (CharacterDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) {
            Text("Rick and Morty", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (filter == null) {
                OutlinedTextField(
                    value = search,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar personaje por nombre") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onPickLocation, modifier = Modifier.weight(1f)) { Text("Localización") }
                    OutlinedButton(onClick = onPickEpisode, modifier = Modifier.weight(1f)) { Text("Episodio") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(filter, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClearFilter) { Text("Quitar filtro") }
                }
            }
        }

        if (filter != null) {
            when {
                busy && filtered.isNullOrEmpty() -> CenterMessage("Cargando personajes…", loading = true)
                error != null -> CenterMessage(error, action = onRetryFilter)
                filtered.isNullOrEmpty() -> CenterMessage("No se encontraron personajes.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { character -> CharacterRow(character, onClick = { onCharacter(character) }) }
                }
            }
        } else {
            when (val state = characters.loadState.refresh) {
                is LoadState.Loading -> if (characters.itemCount == 0) CenterMessage("Cargando personajes…", loading = true)
                is LoadState.Error -> if (characters.itemCount == 0) CenterMessage(state.error.localizedMessage ?: "Error al cargar.", action = { characters.retry() })
                else -> Unit
            }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(characters.itemCount) { index -> characters[index]?.let { CharacterRow(it) { onCharacter(it) } } }
                if (characters.loadState.append is LoadState.Loading) item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                if (characters.loadState.append is LoadState.Error) item {
                    CenterMessage("No se pudieron cargar más personajes.", action = { characters.retry() })
                }
                if (characters.loadState.refresh is LoadState.NotLoading && characters.itemCount == 0) item { CenterMessage("No se encontraron personajes.") }
            }
        }
    }
}

@Composable
private fun CharacterRow(character: CharacterDto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = character.image, contentDescription = character.name, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp).clip(RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)))
            Text(character.name, modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DetailScreen(detail: CharacterDto?, loading: Boolean, error: String?, onRetry: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) { Text("← Volver") }
        when {
            loading && detail == null -> CenterMessage("Cargando personaje…", loading = true)
            error != null && detail == null -> CenterMessage(error, action = onRetry)
            detail != null -> LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    AsyncImage(model = detail.image, contentDescription = detail.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(24.dp)))
                    Spacer(Modifier.height(14.dp))
                    Text(detail.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                item { DetailField("Estado", detail.status) }
                item { DetailField("Especie", detail.species) }
                if (detail.type.isNotBlank()) item { DetailField("Tipo", detail.type) }
                item { DetailField("Género", detail.gender) }
                item { DetailField("Origen", detail.origin.name) }
                item { DetailField("Última localización", detail.location.name) }
                item { DetailField("Aparece en", "${detail.episode.size} episodios") }
                item { DetailField("Creado", detail.created.substringBefore('T')) }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun CenterMessage(message: String, loading: Boolean = false, action: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        if (loading) CircularProgressIndicator(Modifier.padding(bottom = 12.dp))
        Text(message)
        if (action != null) Button(onClick = action, modifier = Modifier.padding(top = 10.dp)) { Text("Reintentar") }
    }
}

@Composable
private fun PickerDialog(title: String, loading: Boolean, error: String?, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            when {
                loading -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Text(error)
                else -> LazyColumn(Modifier.height(440.dp)) { item { content() } }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable
private fun PickerRow(text: String, onClick: () -> Unit) {
    Text(text, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp), style = MaterialTheme.typography.bodyMedium)
}
