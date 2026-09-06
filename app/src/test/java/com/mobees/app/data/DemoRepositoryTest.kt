package com.mobees.app.data

import com.mobees.app.data.demo.DemoRepository
import com.mobees.app.data.model.MediaType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DemoRepositoryTest {

    private val repository = DemoRepository {
        File("src/main/assets/demo_titles.json").readText()
    }

    @Test
    fun `dataset parses and lists titles`() = runTest {
        val movies = repository.trendingMovies()
        val shows = repository.popularTv()
        assertTrue(movies.size >= 10)
        assertTrue(shows.size >= 5)
        assertTrue(movies.all { it.mediaType == MediaType.MOVIE })
        assertTrue(shows.all { it.mediaType == MediaType.TV })
        assertTrue(repository.isDemo)
    }

    @Test
    fun `search matches titles case-insensitively and honours filter`() = runTest {
        val all = repository.search("breaking")
        assertEquals(1, all.size)
        assertEquals("Breaking Bad", all.first().name)

        val moviesOnly = repository.search("breaking", MediaType.MOVIE)
        assertTrue(moviesOnly.isEmpty())

        assertTrue(repository.search("   ").isEmpty())
    }

    @Test
    fun `tv detail exposes every season and episode`() = runTest {
        val show = repository.tv(1396)
        assertEquals("Breaking Bad", show.summary.name)
        assertEquals(5, show.seasons.size)
        assertEquals(62, show.episodeCount)
        val ozymandias = show.seasons[4].episodes.first { it.episodeNumber == 14 }
        assertEquals("Ozymandias", ozymandias.name)
        assertEquals("S05E14", ozymandias.code)
        assertTrue(ozymandias.rating > 9.5)
        assertTrue(show.seasons.all { s -> s.episodes.all { it.isRated } })
    }

    @Test
    fun `movie detail resolves franchise and similar titles`() = runTest {
        val movie = repository.movie(155)
        assertEquals("The Dark Knight", movie.summary.name)
        val franchise = movie.franchise
        assertNotNull(franchise)
        assertEquals(3, franchise!!.entries.size)
        assertEquals(1, franchise.entries.count { it.isCurrent })
        assertEquals(155, franchise.entries.first { it.isCurrent }.id)
        assertFalse(movie.similar.isEmpty())
        assertTrue(movie.directors.contains("Christopher Nolan"))
    }

    @Test(expected = NoSuchElementException::class)
    fun `unknown movie throws`() = runTest {
        repository.movie(-1)
    }
}
