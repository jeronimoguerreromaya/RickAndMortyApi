package com.example.rickandmortyapi.data.paging

import androidx.paging.PagingSource
import com.example.rickandmortyapi.data.remote.PagedResponse
import com.example.rickandmortyapi.data.remote.PageInfo
import com.example.rickandmortyapi.domain.model.CharacterFilter
import com.example.rickandmortyapi.fake.FakeRickAndMortyApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CharacterPagingSourceTest {

    private val api = FakeRickAndMortyApi()

    private fun source(filter: CharacterFilter = CharacterFilter.None) =
        CharacterPagingSource(api, filter)

    @Test
    fun `load returns domain characters and page keys`() = runTest {
        api.characterPages = mapOf(
            1 to PagedResponse(
                info = PageInfo(
                    count = 2,
                    pages = 2,
                    next = "https://rickandmortyapi.com/api/character?page=2",
                    prev = null
                ),
                results = listOf(FakeRickAndMortyApi.characterDto(1, name = "Rick Sanchez"))
            )
        )

        val result = source().load(
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

        val result = source().load(
            PagingSource.LoadParams.Refresh(key = 2, loadSize = 20, placeholdersEnabled = false)
        )

        val page = result as PagingSource.LoadResult.Page
        assertNull(page.nextKey)
        assertEquals(1, page.prevKey)
    }

    @Test
    fun `load forwards name status and species to the api`() = runTest {
        source(CharacterFilter(name = "rick", status = "alive", species = "Human")).load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        )

        assertEquals(listOf(1), api.requestedPages)
        assertEquals("rick", api.lastName)
        assertEquals("alive", api.lastStatus)
        assertEquals("Human", api.lastSpecies)
    }

    @Test
    fun `blank name is not sent to the api`() = runTest {
        source(CharacterFilter(name = "   ")).load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        )

        assertNull(api.lastName)
    }

    @Test
    fun `a 404 response is an empty page instead of an error`() = runTest {
        api.httpErrorCode = 404

        val result = source(CharacterFilter(name = "no-existe")).load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertTrue((result as PagingSource.LoadResult.Page).data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `other http errors are reported as load errors`() = runTest {
        api.httpErrorCode = 500

        val result = source().load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
    }

    @Test
    fun `load returns error when the request fails`() = runTest {
        api.error = IOException("sin red")

        val result = source().load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false)
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals("sin red", (result as PagingSource.LoadResult.Error).throwable.message)
    }

    @Test
    fun `mapping exposes origin last location and episode ids`() = runTest {
        api.characterPages = mapOf(
            1 to PagedResponse(
                info = PageInfo(count = 1, pages = 1, next = null, prev = null),
                results = listOf(
                    FakeRickAndMortyApi.characterDto(
                        3,
                        origin = "Earth (Replacement Dimension)",
                        location = "Interdimensional Customs",
                        episodes = listOf(
                            "https://rickandmortyapi.com/api/episode/1",
                            "https://rickandmortyapi.com/api/episode/2",
                            "https://rickandmortyapi.com/api/episode/3"
                        )
                    )
                )
            )
        )

        val result = source().load(
            PagingSource.LoadParams.Refresh(null, 20, false)
        ) as PagingSource.LoadResult.Page

        val character = result.data.first()
        assertEquals("Earth (Replacement Dimension)", character.originName)
        assertEquals("Interdimensional Customs", character.lastKnownLocation)
        assertEquals(listOf(1, 2, 3), character.episodeIds)
        assertEquals(3, character.episodeCount)
        assertEquals("2017-12-02", character.createdDate)
    }
}
