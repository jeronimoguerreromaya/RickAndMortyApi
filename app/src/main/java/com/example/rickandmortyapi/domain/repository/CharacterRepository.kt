package com.example.rickandmortyapi.domain.repository

import androidx.paging.PagingData
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.CharacterFilter
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los datos de personajes. La capa de datos implementa
 * esta interfaz; la capa de dominio (casos de uso) solo depende de ella.
 */
interface CharacterRepository {

    /** Listado paginado de personajes, filtrado por nombre/estado/especie. */
    fun characters(filter: CharacterFilter): Flow<PagingData<Character>>

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

    /** Episodios concretos a partir de sus ids (para el detalle del personaje). */
    suspend fun episodesByIds(ids: List<Int>): List<Episode>
}
