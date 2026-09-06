package com.mobees.app.data.remote

import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.data.model.TvDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TmdbRepository(private val api: TmdbApi) : TitleRepository {

    override val isDemo: Boolean = false

    override suspend fun trendingMovies(): List<TitleSummary> =
        api.trendingMovies().results.mapNotNull { it.toSummary(MediaType.MOVIE) }

    override suspend fun popularTv(): List<TitleSummary> =
        api.popularTv().results.mapNotNull { it.toSummary(MediaType.TV) }

    override suspend fun topRated(): List<TitleSummary> = coroutineScope {
        val movies = async { api.topRatedMovies().results.mapNotNull { it.toSummary(MediaType.MOVIE) } }
        val tv = async { api.topRatedTv().results.mapNotNull { it.toSummary(MediaType.TV) } }
        (movies.await() + tv.await()).sortedByDescending { it.rating }.take(20)
    }

    override suspend fun search(query: String, filter: MediaType?): List<TitleSummary> {
        if (query.isBlank()) return emptyList()
        val results = when (filter) {
            MediaType.MOVIE -> api.searchMovies(query).results.mapNotNull { it.toSummary(MediaType.MOVIE) }
            MediaType.TV -> api.searchTv(query).results.mapNotNull { it.toSummary(MediaType.TV) }
            null -> api.searchMulti(query).results.mapNotNull { it.toSummary() }
        }
        return results.sortedByDescending { it.voteCount }
    }

    override suspend fun movie(id: Int): MovieDetail {
        val dto = api.movie(id)
        val collection = dto.belongsToCollection?.let { ref ->
            runCatching { api.collection(ref.id) }.getOrNull()
        }
        return dto.toMovieDetail(collection)
    }

    override suspend fun tv(id: Int): TvDetail = coroutineScope {
        val dto = api.tv(id)
        val seasons = dto.seasons
            .filter { it.seasonNumber > 0 && it.episodeCount > 0 }
            .map { ref -> async { runCatching { api.season(id, ref.seasonNumber).toSeason() }.getOrNull() } }
            .mapNotNull { it.await() }
        dto.toTvDetail(seasons)
    }
}
