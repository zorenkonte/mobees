package com.mobees.app.domain

import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.Season

data class SeasonStats(
    val seasonNumber: Int,
    val episodeCount: Int,
    val ratedCount: Int,
    val average: Double,
    val best: Episode?,
    val worst: Episode?,
)

data class SeriesOverview(
    val average: Double,
    val ratedEpisodes: Int,
    val totalEpisodes: Int,
    val best: Episode?,
    val worst: Episode?,
    val seasons: List<SeasonStats>,
    val maxEpisodesInSeason: Int,
) {
    val hasRatings: Boolean get() = ratedEpisodes > 0
}

/** Pure functions over seasons/episodes that back the series graph. */
object SeriesStats {

    fun ratedEpisodes(seasons: List<Season>): List<Episode> =
        seasons.flatMap { it.episodes }.filter { it.isRated }

    fun average(episodes: List<Episode>): Double {
        val rated = episodes.filter { it.isRated }
        return if (rated.isEmpty()) 0.0 else rated.sumOf { it.rating } / rated.size
    }

    fun seasonStats(season: Season): SeasonStats {
        val rated = season.episodes.filter { it.isRated }
        return SeasonStats(
            seasonNumber = season.seasonNumber,
            episodeCount = season.episodes.size,
            ratedCount = rated.size,
            average = average(season.episodes),
            best = rated.maxByOrNull { it.rating },
            worst = rated.minByOrNull { it.rating },
        )
    }

    fun overview(seasons: List<Season>): SeriesOverview {
        val all = seasons.flatMap { it.episodes }
        val rated = all.filter { it.isRated }
        return SeriesOverview(
            average = average(all),
            ratedEpisodes = rated.size,
            totalEpisodes = all.size,
            best = rated.maxByOrNull { it.rating },
            worst = rated.minByOrNull { it.rating },
            seasons = seasons.map { seasonStats(it) },
            maxEpisodesInSeason = seasons.maxOfOrNull { s -> s.episodes.maxOfOrNull { it.episodeNumber } ?: 0 } ?: 0,
        )
    }
}
