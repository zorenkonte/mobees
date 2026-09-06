package com.mobees.app.data.ratings

import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.RatingSource
import com.mobees.app.data.model.Season
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.data.model.TvDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RatedTitleRepositoryTest {

    private fun summary(type: MediaType, imdbId: String? = "tt0903747") = TitleSummary(
        id = 1, mediaType = type, name = "Title", overview = "", posterUrl = null, backdropUrl = null,
        year = "2008", rating = 8.9, voteCount = 1000, imdbId = imdbId,
    )

    private fun episode(season: Int, number: Int, rating: Double) = Episode(
        seasonNumber = season, episodeNumber = number, name = "E$number", overview = "", airDate = null,
        rating = rating, voteCount = 50, stillUrl = null,
    )

    private fun movie(imdbId: String? = "tt0903747") = MovieDetail(
        summary = summary(MediaType.MOVIE, imdbId), tagline = null, runtimeMinutes = 100, releaseDate = null,
        genres = emptyList(), cast = emptyList(), directors = emptyList(), franchise = null, similar = emptyList(),
    )

    private fun tv(imdbId: String? = "tt0903747") = TvDetail(
        summary = summary(MediaType.TV, imdbId), tagline = null, firstAirDate = null, lastAirDate = null, status = null,
        episodeRuntimeMinutes = null, genres = emptyList(), cast = emptyList(), creators = emptyList(), networks = emptyList(),
        seasons = listOf(
            Season(1, "Season 1", null, null, listOf(episode(1, 1, 8.0), episode(1, 2, 8.1))),
            Season(2, "Season 2", null, null, listOf(episode(2, 1, 8.2), episode(2, 2, 8.3), episode(2, 3, 8.4))),
        ),
    )

    private class FakeBase(private val movie: MovieDetail, private val tv: TvDetail) : TitleRepository {
        override val isDemo = true
        override suspend fun trendingMovies() = emptyList<TitleSummary>()
        override suspend fun popularTv() = emptyList<TitleSummary>()
        override suspend fun topRated() = emptyList<TitleSummary>()
        override suspend fun search(query: String, filter: MediaType?) = emptyList<TitleSummary>()
        override suspend fun movie(id: Int) = movie
        override suspend fun tv(id: Int) = tv
    }

    private class FakeImdb(
        private val title: ImdbRating? = ImdbRating(9.5, 2_100_000),
        private val seasons: Map<Int, Map<Int, Double>> = mapOf(1 to mapOf(1 to 9.0, 2 to 9.1), 2 to mapOf(1 to 9.2, 3 to 9.9)),
        private val failure: Exception? = null,
    ) : ImdbRatingsSource {
        var titleCalls = 0
        override suspend fun title(imdbId: String): ImdbRating? { titleCalls++; failure?.let { throw it }; return title }
        override suspend fun season(imdbId: String, season: Int): Map<Int, Double> { failure?.let { throw it }; return seasons[season].orEmpty() }
    }

    @Test
    fun `movie gets IMDb rating when provider is OMDb`() = runTest {
        val repo = RatedTitleRepository(FakeBase(movie(), tv()), FakeImdb()) { RatingsProvider.OMDB }
        val rated = repo.movie(1)
        assertNull(rated.notice)
        assertEquals(9.5, rated.detail.summary.rating, 0.0001)
        assertEquals(2_100_000, rated.detail.summary.voteCount)
        assertEquals(RatingSource.IMDB, rated.detail.summary.ratingSource)
        assertEquals(8.9, rated.detail.tmdbRating, 0.0001)
    }

    @Test
    fun `provider TMDB leaves base data untouched and never calls OMDb`() = runTest {
        val imdb = FakeImdb()
        val repo = RatedTitleRepository(FakeBase(movie(), tv()), imdb) { RatingsProvider.TMDB }
        val rated = repo.tv(1)
        assertNull(rated.notice)
        assertEquals(RatingSource.TMDB, rated.detail.summary.ratingSource)
        assertEquals(8.9, rated.detail.summary.rating, 0.0001)
        assertEquals(0, imdb.titleCalls)
    }

    @Test
    fun `tv episodes are overlaid by number and unmatched ones become unrated`() = runTest {
        val repo = RatedTitleRepository(FakeBase(movie(), tv()), FakeImdb()) { RatingsProvider.OMDB }
        val show = repo.tv(1).detail
        val s2 = show.seasons.first { it.seasonNumber == 2 }.episodes
        assertEquals(9.2, s2[0].rating, 0.0001)
        assertFalse(s2[1].isRated)
        assertEquals(9.9, s2[2].rating, 0.0001)
        assertTrue(show.seasons.flatMap { it.episodes }.all { it.ratingSource == RatingSource.IMDB })
        assertEquals(RatingSource.IMDB, show.summary.ratingSource)
    }

    @Test
    fun `source failure falls back to TMDB ratings with a notice`() = runTest {
        val repo = RatedTitleRepository(
            FakeBase(movie(), tv()),
            FakeImdb(failure = OmdbException("Request limit reached!")),
        ) { RatingsProvider.OMDB }
        val rated = repo.tv(1)
        assertNotNull(rated.notice)
        assertTrue(rated.notice!!.contains("Request limit reached!"))
        assertEquals(RatingSource.TMDB, rated.detail.summary.ratingSource)
        assertEquals(8.0, rated.detail.seasons[0].episodes[0].rating, 0.0001)
    }

    @Test
    fun `missing imdb id and unrated titles produce notices`() = runTest {
        val noId = RatedTitleRepository(FakeBase(movie(imdbId = null), tv()), FakeImdb()) { RatingsProvider.OMDB }
        assertEquals(RatedTitleRepository.NO_IMDB_ID, noId.movie(1).notice)

        val unrated = RatedTitleRepository(FakeBase(movie(), tv()), FakeImdb(title = null)) { RatingsProvider.OMDB }
        assertEquals(RatedTitleRepository.NOT_RATED, unrated.movie(1).notice)
    }

    @Test
    fun `no source configured passes through silently`() = runTest {
        val repo = RatedTitleRepository(FakeBase(movie(), tv()), null) { RatingsProvider.OMDB }
        val rated = repo.movie(1)
        assertNull(rated.notice)
        assertEquals(RatingSource.TMDB, rated.detail.summary.ratingSource)
    }
}
