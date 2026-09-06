package com.mobees.app.data.ratings

import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.RatingSource
import com.mobees.app.data.model.Season
import com.mobees.app.data.model.TvDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** A detail result plus an optional one-line notice explaining a ratings fallback. */
data class Rated<T>(val detail: T, val notice: String? = null)

/**
 * Decorates [TitleRepository] so detail screens can show IMDb ratings (via OMDb) while the
 * catalogue itself still comes from the base repository. Any failure falls back to the base
 * ratings with a notice instead of failing the screen.
 */
class RatedTitleRepository(
    private val base: TitleRepository,
    private val imdb: ImdbRatingsSource?,
    private val provider: suspend () -> RatingsProvider,
) {

    suspend fun movie(id: Int): Rated<MovieDetail> {
        val detail = base.movie(id)
        val source = activeSource() ?: return Rated(detail)
        val imdbId = detail.summary.imdbId ?: return Rated(detail, NO_IMDB_ID)
        return try {
            val rating = source.title(imdbId) ?: return Rated(detail, NOT_RATED)
            Rated(
                detail.copy(
                    summary = detail.summary.copy(
                        rating = rating.rating,
                        voteCount = rating.votes ?: 0,
                        ratingSource = RatingSource.IMDB,
                    ),
                ),
            )
        } catch (e: Exception) {
            Rated(detail, fallbackNotice(e))
        }
    }

    suspend fun tv(id: Int): Rated<TvDetail> {
        val detail = base.tv(id)
        val source = activeSource() ?: return Rated(detail)
        val imdbId = detail.summary.imdbId ?: return Rated(detail, NO_IMDB_ID)
        return try {
            coroutineScope {
                val title = async { source.title(imdbId) }
                val seasons = detail.seasons.map { season ->
                    async { season.seasonNumber to source.season(imdbId, season.seasonNumber) }
                }.awaitAll().toMap()
                val rating = title.await()
                val summary = if (rating != null) {
                    detail.summary.copy(rating = rating.rating, voteCount = rating.votes ?: 0, ratingSource = RatingSource.IMDB)
                } else {
                    detail.summary
                }
                val ratedSeasons = detail.seasons.map { it.overlay(seasons[it.seasonNumber].orEmpty()) }
                val anyEpisodeRated = ratedSeasons.any { s -> s.episodes.any { it.isRated } }
                Rated(
                    detail.copy(summary = summary, seasons = ratedSeasons),
                    notice = if (rating == null && !anyEpisodeRated) NOT_RATED else null,
                )
            }
        } catch (e: Exception) {
            Rated(detail, fallbackNotice(e))
        }
    }

    private suspend fun activeSource(): ImdbRatingsSource? =
        if (provider() == RatingsProvider.OMDB) imdb else null

    private fun Season.overlay(ratings: Map<Int, Double>): Season = copy(
        episodes = episodes.map { ep ->
            ep.copy(
                rating = ratings[ep.episodeNumber] ?: 0.0,
                voteCount = 0,
                ratingSource = RatingSource.IMDB,
            )
        },
    )

    private fun fallbackNotice(e: Exception): String =
        "IMDb ratings unavailable (${e.message ?: e.javaClass.simpleName}). Showing TMDB ratings."

    companion object {
        const val NO_IMDB_ID = "No IMDb id for this title. Showing TMDB ratings."
        const val NOT_RATED = "IMDb has not rated this title yet. Showing TMDB ratings."
    }
}
