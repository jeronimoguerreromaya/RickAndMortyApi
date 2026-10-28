package com.example.rickandmortyapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rickandmortyapi.ui.RickAndMortyApp
import com.example.rickandmortyapi.ui.theme.RickAndMortyApiTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Unica Activity de la aplicacion. Compose se encarga del resto de la navegacion.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickAndMortyApiTheme {
                RickAndMortyApp()
            }
        }
    }
}
