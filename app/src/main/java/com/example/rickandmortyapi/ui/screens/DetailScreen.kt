package com.example.rickandmortyapi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.ui.components.StatusMessage

/**
 * Pantalla de detalle de un personaje, alimentada por GET /character/{id}.
 */
@Composable
fun DetailScreen(
    detail: Character?,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
            Text("← Volver")
        }
        when {
            loading && detail == null -> StatusMessage("Cargando personaje…", loading = true)
            error != null && detail == null -> StatusMessage(error, action = onRetry)
            detail != null -> CharacterDetail(detail)
        }
    }
}

@Composable
private fun CharacterDetail(character: Character) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
            )
            Spacer(Modifier.height(14.dp))
            Text(
                character.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item { DetailField("Estado", character.status) }
        item { DetailField("Especie", character.species) }
        if (character.type.isNotBlank()) {
            item { DetailField("Tipo", character.type) }
        }
        item { DetailField("Género", character.gender) }
        item { DetailField("Origen", character.originName) }
        item { DetailField("Última localización", character.lastKnownLocation) }
        item { DetailField("Aparece en", "${character.episodeCount} episodios") }
        item { DetailField("Creado", character.createdDate) }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
