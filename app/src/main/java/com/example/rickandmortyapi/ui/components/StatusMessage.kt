package com.example.rickandmortyapi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Mensaje centrado para estados vacios, de carga o de error, con accion opcional.
 */
@Composable
fun StatusMessage(
    message: String,
    loading: Boolean = false,
    action: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 12.dp))
        }
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (action != null) {
            Button(onClick = action, modifier = Modifier.padding(top = 10.dp)) {
                Text("Reintentar")
            }
        }
    }
}
