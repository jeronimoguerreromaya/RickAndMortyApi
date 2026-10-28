package com.example.rickandmortyapi.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmortyapi.data.mapper.toDomainCharacters
import com.example.rickandmortyapi.data.remote.RickAndMortyApi
import com.example.rickandmortyapi.domain.model.Character

/**
 * Fuente de datos paginada: pide las paginas de personajes a la API REST
 * y devuelve modelos de dominio listos para la UI.
 */
class CharacterPagingSource(
    private val api: RickAndMortyApi,
    private val name: String?
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> = try {
        val page = params.key ?: 1
        val response = api.characters(page, name?.takeIf { it.isNotBlank() })
        LoadResult.Page(
            data = response.results.toDomainCharacters(),
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (response.info.next == null) null else page + 1
        )
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
}
