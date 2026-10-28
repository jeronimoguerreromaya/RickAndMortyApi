package com.example.rickandmortyapi.data.mapper

import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.EpisodeDto
import com.example.rickandmortyapi.data.remote.LocationDto
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.Episode
import com.example.rickandmortyapi.domain.model.Location

/**
 * Mapeos de la capa de datos (DTOs de la API) a la capa de dominio.
 * La UI nunca ve los DTOs: solo los modelos de dominio.
 */

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    originName = origin.name,
    lastKnownLocation = location.name,
    imageUrl = image,
    episodeCount = episode.size,
    createdDate = created.substringBefore('T')
)

fun LocationDto.toDomain(): Location = Location(
    id = id,
    name = name,
    type = type,
    dimension = dimension
)

fun EpisodeDto.toDomain(): Episode = Episode(
    id = id,
    name = name,
    airDate = air_date,
    code = episode
)

fun List<CharacterDto>.toDomainCharacters(): List<Character> = map { it.toDomain() }

fun List<LocationDto>.toDomainLocations(): List<Location> = map { it.toDomain() }

fun List<EpisodeDto>.toDomainEpisodes(): List<Episode> = map { it.toDomain() }
