package com.example.rickandmortyapi.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmortyapi.data.remote.CharacterDto
import com.example.rickandmortyapi.data.remote.RickAndMortyApi

class CharacterPagingSource(private val api: RickAndMortyApi, private val name: String?) : PagingSource<Int, CharacterDto>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CharacterDto> = try {
        val page = params.key ?: 1
        val response = api.characters(page, name?.takeIf { it.isNotBlank() })
        LoadResult.Page(response.results, if (page == 1) null else page - 1, if (response.info.next == null) null else page + 1)
    } catch (error: Exception) {
        LoadResult.Error(error)
    }

    override fun getRefreshKey(state: PagingState<Int, CharacterDto>): Int? = state.anchorPosition?.let { anchor ->
        state.closestPageToPosition(anchor)?.prevKey?.plus(1) ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
    }
}
