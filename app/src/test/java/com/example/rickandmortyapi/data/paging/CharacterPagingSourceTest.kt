package com.example.rickandmortyapi.data.paging

import androidx.paging.PagingSource
import com.example.rickandmortyapi.data.remote.PagedResponse
import com.example.rickandmortyapi.data.remote.PageInfo
import com.example.rickandmortyapi.fake.FakeRickAndMortyApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CharacterPagingSourceTest {

    private val api = FakeRickAndMortyApi()

    @Test
    fun `load returns domain characters and page keys`() = runTest {
        api.characterPages = mapOf(
            1 to PagedResponse(
                info = PageInfo(count = 2, pages = 2, next = "https://rickandmortyapi.com/api/character?page=2", prev = null),
                results = listOf(FakeRickAndMortyApi.characterDto(1, name = "Rick Sanchez"))
            )
        )
        val source = CharacterPagingSource(api, name = null)

        val result = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals("Rick Sanchez", page.data.first().name)
        assertEquals("https://example.com/1.png", page.data.first().imageUrl)
        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load stops pagination on the last page`() = runTest {
        api.characterPages = mapOf(
            2 to PagedResponse(
                info = PageInfo(count = 1, pages = 2, next = null, prev = null),
                results = listOf(FakeRickAndMortyApi.characterDto(2))
            )
        )
        val source = CharacterPagingSource(api, name = null)

        val result = source.load(
            PagingSource.LoadParams.Refresh(key = 2, loadSize = 20, placeholdersEnabled = false)
        )

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
        assertEquals(1, page.prevKey)
    }

    @Test
    fun `load sends the name filter to the api`() = runTest {
        val source = CharacterPagingSource(api, name = "rick")

        source.load(PagingSource.LoadParams.Refresh(null, 20, false))

        assertEquals(listOf(1), api.requestedPages)
    }

    @Test
    fun `load returns error when the request fails`() = runTest {
        api.error = IOException("sin red")
        val source = CharacterPagingSource(api, name = null)

        val result = source.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("sin red", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun `mapping exposes origin and last location names`() = runTest {
        api.characterPages = mapOf(
            1 to PagedResponse(
                info = PageInfo(count = 1, pages = 1, next = null, prev = null),
                results = listOf(
                    FakeRickAndMortyApi.characterDto(
                        3,
                        origin = "Earth (Replacement Dimension)",
                        location = "Interdimensional Customs"
                    )
                )
            )
        )
        val source = CharacterPagingSource(api, name = null)

        val result = source.load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        ) as PagingSource.LoadResult.Page

        assertEquals("Earth (Replacement Dimension)", result.data.first().originName)
        assertEquals("Interdimensional Customs", result.data.first().lastKnownLocation)
        assertEquals(1, result.data.first().episodeCount)
        assertEquals("2017-12-02", result.data.first().createdDate)
    }
}
