package com.example.rickandmortyapi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada de la aplicacion. Hilt crea aqui el grafo de dependencias
 * (inyeccion de dependencias) que usan las capas ViewModel, Repository y Data.
 */
@HiltAndroidApp
class RickAndMortyApplication : Application()
