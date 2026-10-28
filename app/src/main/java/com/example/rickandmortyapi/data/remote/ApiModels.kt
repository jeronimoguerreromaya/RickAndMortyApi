package com.example.rickandmortyapi.data.remote

data class PagedResponse<T>(val info: PageInfo, val results: List<T>)
data class PageInfo(val count: Int, val pages: Int, val next: String?, val prev: String?)
data class NamedReference(val name: String, val url: String)

data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: NamedReference,
    val location: NamedReference,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

data class LocationDto(val id: Int, val name: String, val type: String, val dimension: String, val residents: List<String>, val url: String)
data class EpisodeDto(val id: Int, val name: String, val air_date: String, val episode: String, val characters: List<String>, val url: String)
