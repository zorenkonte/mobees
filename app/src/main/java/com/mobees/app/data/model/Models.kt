package com.mobees.app.data.model

enum class MediaType { MOVIE, TV }

/** Where a rating value came from. */
enum class RatingSource(val label: String) { TMDB("TMDB"), IMDB("IMDb") }

/** Lightweight representation used in lists, carousels and search results. */
data class TitleSummary(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val year: String?,
    val rating: Double,
    val voteCount: Int,
    val imdbId: String? = null,
    val ratingSource: RatingSource = RatingSource.TMDB,
)

data class Genre(val id: Int, val name: String)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
)

data class FranchiseEntry(
    val id: Int,
    val title: String,
    val year: String?,
    val rating: Double,
    val isCurrent: Boolean,
    val ratingSource: RatingSource = RatingSource.TMDB,
)

data class Franchise(val name: String, val entries: List<FranchiseEntry>)

data class MovieDetail(
    val summary: TitleSummary,
    val tagline: String?,
    val runtimeMinutes: Int?,
    val releaseDate: String?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val directors: List<String>,
    val franchise: Franchise?,
    val similar: List<TitleSummary>,
    /** TMDB rating kept for like-for-like comparisons even when the summary shows IMDb. */
    val tmdbRating: Double = summary.rating,
)

data class Episode(
    val seasonNumber: Int,
    val episodeNumber: Int,
    val name: String,
    val overview: String,
    val airDate: String?,
    val rating: Double,
    val voteCount: Int,
    val stillUrl: String?,
    val ratingSource: RatingSource = RatingSource.TMDB,
) {
    val code: String get() = "S%02dE%02d".format(seasonNumber, episodeNumber)
    val isRated: Boolean get() = rating > 0.0
}

data class Season(
    val seasonNumber: Int,
    val name: String,
    val airDate: String?,
    val posterUrl: String?,
    val episodes: List<Episode>,
)

data class TvDetail(
    val summary: TitleSummary,
    val tagline: String?,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val status: String?,
    val episodeRuntimeMinutes: Int?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val creators: List<String>,
    val networks: List<String>,
    val seasons: List<Season>,
) {
    val episodeCount: Int get() = seasons.sumOf { it.episodes.size }
}

fun String?.toYear(): String? = this?.takeIf { it.length >= 4 }?.substring(0, 4)
