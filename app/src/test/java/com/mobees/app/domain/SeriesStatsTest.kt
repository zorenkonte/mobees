package com.mobees.app.domain

import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.Season
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesStatsTest {

    private fun episode(season: Int, number: Int, rating: Double, votes: Int = 100) = Episode(
        seasonNumber = season,
        episodeNumber = number,
        name = "S${season}E$number",
        overview = "",
        airDate = null,
        rating = rating,
        voteCount = votes,
        stillUrl = null,
    )

    private val seasons = listOf(
        Season(1, "Season 1", null, null, listOf(episode(1, 1, 8.0), episode(1, 2, 9.0), episode(1, 3, 0.0, votes = 0))),
        Season(2, "Season 2", null, null, listOf(episode(2, 1, 6.0), episode(2, 2, 7.0), episode(2, 3, 8.0), episode(2, 4, 5.0))),
    )

    @Test
    fun `average ignores unrated episodes`() {
        assertEquals(8.5, SeriesStats.average(seasons[0].episodes), 0.0001)
    }

    @Test
    fun `season stats identify best and worst`() {
        val stats = SeriesStats.seasonStats(seasons[1])
        assertEquals(4, stats.episodeCount)
        assertEquals(4, stats.ratedCount)
        assertEquals(6.5, stats.average, 0.0001)
        assertEquals(3, stats.best?.episodeNumber)
        assertEquals(4, stats.worst?.episodeNumber)
    }

    @Test
    fun `overview aggregates across seasons`() {
        val overview = SeriesStats.overview(seasons)
        assertEquals(7, overview.totalEpisodes)
        assertEquals(6, overview.ratedEpisodes)
        assertEquals((8.0 + 9.0 + 6.0 + 7.0 + 8.0 + 5.0) / 6, overview.average, 0.0001)
        assertEquals("S01E02", overview.best?.code)
        assertEquals("S02E04", overview.worst?.code)
        assertEquals(4, overview.maxEpisodesInSeason)
        assertEquals(2, overview.seasons.size)
        assertTrue(overview.hasRatings)
    }

    @Test
    fun `empty input produces empty overview`() {
        val overview = SeriesStats.overview(emptyList())
        assertEquals(0.0, overview.average, 0.0)
        assertNull(overview.best)
        assertFalse(overview.hasRatings)
        assertEquals(0, overview.maxEpisodesInSeason)
    }
}
