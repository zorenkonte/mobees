package com.mobees.app.data.demo

import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.CastMember
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.Franchise
import com.mobees.app.data.model.FranchiseEntry
import com.mobees.app.data.model.Genre
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.Season
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.data.model.TvDetail
import com.mobees.app.data.model.toYear
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Serves the bundled sample dataset. The JSON is supplied through [jsonProvider] so the
 * repository can be exercised on the JVM without Android assets.
 */
class DemoRepository(private val jsonProvider: suspend () -> String) : TitleRepository {

    override val isDemo: Boolean = true

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    private val mutex = Mutex()
    private var cached: DemoDataset? = null

    private suspend fun dataset(): DemoDataset = mutex.withLock {
        cached ?: withContext(Dispatchers.Default) {
            json.decodeFromString(DemoDataset.serializer(), jsonProvider())
        }.also { cached = it }
    }

    override suspend fun trendingMovies(): List<TitleSummary> = dataset().movies
        .sortedWith(compareByDescending<DemoMovie> { it.trending }.thenByDescending { it.voteCount })
        .map { it.toSummary() }

    override suspend fun popularTv(): List<TitleSummary> = dataset().shows
        .sortedWith(compareByDescending<DemoShow> { it.popular }.thenByDescending { it.voteCount })
        .map { it.toSummary() }

    override suspend fun topRated(): List<TitleSummary> {
        val data = dataset()
        return (data.movies.map { it.toSummary() } + data.shows.map { it.toSummary() })
            .sortedByDescending { it.rating }
    }

    override suspend fun search(query: String, filter: MediaType?): List<TitleSummary> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val data = dataset()
        val movies = if (filter == MediaType.TV) emptyList() else data.movies
            .filter { it.title.contains(q, ignoreCase = true) || it.genres.any { g -> g.equals(q, ignoreCase = true) } }
            .map { it.toSummary() }
        val shows = if (filter == MediaType.MOVIE) emptyList() else data.shows
            .filter { it.name.contains(q, ignoreCase = true) || it.genres.any { g -> g.equals(q, ignoreCase = true) } }
            .map { it.toSummary() }
        return (movies + shows).sortedByDescending { it.voteCount }
    }

    override suspend fun movie(id: Int): MovieDetail {
        val data = dataset()
        val movie = data.movies.firstOrNull { it.id == id } ?: throw NoSuchElementException("Movie $id not found")
        val similar = movie.similarIds.mapNotNull { sid -> data.movies.firstOrNull { it.id == sid }?.toSummary() }
        return MovieDetail(
            summary = movie.toSummary(),
            tagline = movie.tagline,
            runtimeMinutes = movie.runtime,
            releaseDate = movie.releaseDate,
            genres = movie.genres.map { it.toGenre() },
            cast = movie.cast.mapIndexed { index, c -> CastMember(index, c.name, c.character, null) },
            directors = movie.directors,
            franchise = movie.franchise?.let { f ->
                Franchise(
                    name = f.name,
                    entries = f.entries.map { e -> FranchiseEntry(e.id, e.title, e.year, e.rating, e.id == movie.id) },
                )
            },
            similar = similar,
        )
    }

    override suspend fun tv(id: Int): TvDetail {
        val show = dataset().shows.firstOrNull { it.id == id } ?: throw NoSuchElementException("Show $id not found")
        return TvDetail(
            summary = show.toSummary(),
            tagline = show.tagline,
            firstAirDate = show.firstAirDate,
            lastAirDate = show.lastAirDate,
            status = show.status,
            episodeRuntimeMinutes = show.episodeRuntime,
            genres = show.genres.map { it.toGenre() },
            cast = show.cast.mapIndexed { index, c -> CastMember(index, c.name, c.character, null) },
            creators = show.creators,
            networks = show.networks,
            seasons = show.seasons.sortedBy { it.number }.map { s ->
                Season(
                    seasonNumber = s.number,
                    name = s.name ?: "Season ${s.number}",
                    airDate = s.airDate,
                    posterUrl = null,
                    episodes = s.episodes.sortedBy { it.number }.map { e ->
                        Episode(
                            seasonNumber = s.number,
                            episodeNumber = e.number,
                            name = e.name,
                            overview = e.overview,
                            airDate = e.airDate,
                            rating = e.rating,
                            voteCount = e.voteCount,
                            stillUrl = null,
                        )
                    },
                )
            },
        )
    }

    private fun String.toGenre() = Genre(hashCode(), this)

    private fun DemoMovie.toSummary() = TitleSummary(
        id = id,
        mediaType = MediaType.MOVIE,
        name = title,
        overview = overview,
        posterUrl = null,
        backdropUrl = null,
        year = releaseDate.toYear(),
        rating = rating,
        voteCount = voteCount,
        imdbId = imdbId,
    )

    private fun DemoShow.toSummary() = TitleSummary(
        id = id,
        mediaType = MediaType.TV,
        name = name,
        overview = overview,
        posterUrl = null,
        backdropUrl = null,
        year = firstAirDate.toYear(),
        rating = rating,
        voteCount = voteCount,
        imdbId = imdbId,
    )
}
