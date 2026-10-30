package com.example.rickandmortyapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmortyapi.data.mapper.toDomainCharacters
import com.example.rickandmortyapi.data.remote.RickAndMortyApi
import com.example.rickandmortyapi.domain.model.Character
import com.example.rickandmortyapi.domain.model.CharacterFilter
import retrofit2.HttpException

/**
 * Fuente de datos paginada: pide las paginas de personajes a la API REST
 * aplicando los filtros activos y devuelve modelos de dominio.
 */
class CharacterPagingSource(
    private val api: RickAndMortyApi,
    private val filter: CharacterFilter
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> = try {
        val page = params.key ?: 1
        val response = api.characters(
            page = page,
            name = filter.name?.takeIf { it.isNotBlank() },
            status = filter.status,
            species = filter.species
        )
        LoadResult.Page(
            data = response.results.toDomainCharacters(),
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (response.info.next == null) null else page + 1
        )
    } catch (error: HttpException) {
        // La API responde 404 cuando ningun personaje coincide con el filtro:
        // eso es una lista vacia, no un error de red.
        if (error.code() == HTTP_NOT_FOUND) {
            LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
        } else {
            LoadResult.Error(error)
        }
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
