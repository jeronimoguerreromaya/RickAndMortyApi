package com.example.rickandmortyapi.domain.repository

import androidx.paging.PagingData
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los datos de personajes. La capa de datos implementa
 * esta interfaz; la capa de dominio (casos de uso) solo depende de ella.
 */
interface CharacterRepository {

    /** Listado paginado de personajes, opcionalmente filtrado por nombre. */
    fun characters(name: String?): Flow<PagingData<Character>>

    /** Todas las localizaciones disponibles para filtrar. */
    suspend fun allLocations(): List<Location>

    /** Todos los episodios disponibles para filtrar. */
    suspend fun allEpisodes(): List<Episode>

    /** Personajes que residen en una localizacion. */
    suspend fun charactersForLocation(locationId: Int): List<Character>

    /** Personajes que aparecen en un episodio. */
    suspend fun charactersForEpisode(episodeId: Int): List<Character>

    /** Detalle completo de un personaje. */
    suspend fun character(id: Int): Character
}
