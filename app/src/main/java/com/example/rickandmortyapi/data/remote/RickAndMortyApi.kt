package com.example.rickandmortyapi.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApi {
    @GET("character")
    suspend fun characters(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("species") species: String? = null
    ): PagedResponse<CharacterDto>

    @GET("character/{ids}")
    suspend fun charactersByIds(@Path("ids") ids: String): List<CharacterDto>

    @GET("character/{id}")
    suspend fun character(@Path("id") id: Int): CharacterDto

    @GET("location")
    suspend fun locations(@Query("page") page: Int): PagedResponse<LocationDto>

    @GET("location/{id}")
    suspend fun location(@Path("id") id: Int): LocationDto

    @GET("episode")
    suspend fun episodes(@Query("page") page: Int): PagedResponse<EpisodeDto>

    @GET("episode/{id}")
    suspend fun episode(@Path("id") id: Int): EpisodeDto

    @GET("episode/{ids}")
    suspend fun episodesByIds(@Path("ids") ids: String): List<EpisodeDto>
}
