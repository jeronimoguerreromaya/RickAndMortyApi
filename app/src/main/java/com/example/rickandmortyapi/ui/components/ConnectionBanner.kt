package com.example.rickandmortyapi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Indicador obligatorio del estado de conexion (online/offline).
 */
@Composable
fun ConnectionBanner(isOnline: Boolean) {
    val container = if (isOnline) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val content = if (isOnline) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = container, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isOnline) "Conectado a internet" else "Sin conexión a internet",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = content,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
