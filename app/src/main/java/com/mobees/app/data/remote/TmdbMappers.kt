package com.mobees.app.data.remote

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

object TmdbImages {
    private const val BASE = "https://image.tmdb.org/t/p/"

    fun poster(path: String?): String? = path?.let { "${BASE}w500$it" }
    fun backdrop(path: String?): String? = path?.let { "${BASE}w1280$it" }
    fun profile(path: String?): String? = path?.let { "${BASE}w185$it" }
    fun still(path: String?): String? = path?.let { "${BASE}w300$it" }
}

/** Maps a TMDB list item; returns null for people or unknown media types. */
fun TmdbTitleDto.toSummary(defaultType: MediaType? = null): TitleSummary? {
    val type = when (mediaType) {
        "movie" -> MediaType.MOVIE
        "tv" -> MediaType.TV
        null -> defaultType ?: if (title != null) MediaType.MOVIE else MediaType.TV
        else -> return null
    }
    return TitleSummary(
        id = id,
        mediaType = type,
        name = title ?: name ?: "Untitled",
        overview = overview.orEmpty(),
        posterUrl = TmdbImages.poster(posterPath),
        backdropUrl = TmdbImages.backdrop(backdropPath),
        year = (releaseDate ?: firstAirDate).toYear(),
        rating = voteAverage,
        voteCount = voteCount,
    )
}

fun CastDto.toCastMember() = CastMember(
    id = id,
    name = name,
    character = character.orEmpty(),
    profileUrl = TmdbImages.profile(profilePath),
)

fun GenreDto.toGenre() = Genre(id, name)

fun MovieDetailDto.toMovieDetail(collection: CollectionDto?): MovieDetail {
    val summary = TitleSummary(
        id = id,
        mediaType = MediaType.MOVIE,
        name = title,
        overview = overview.orEmpty(),
        posterUrl = TmdbImages.poster(posterPath),
        backdropUrl = TmdbImages.backdrop(backdropPath),
        year = releaseDate.toYear(),
        rating = voteAverage,
        voteCount = voteCount,
        imdbId = externalIds?.imdbId?.takeIf { it.isNotBlank() },
    )
    val franchise = collection?.let { col ->
        Franchise(
            name = col.name,
            entries = col.parts
                .filter { it.voteCount > 0 || it.id == id }
                .sortedBy { it.releaseDate ?: "9999" }
                .map { part ->
                    FranchiseEntry(
                        id = part.id,
                        title = part.title ?: part.name ?: "Untitled",
                        year = part.releaseDate.toYear(),
                        rating = part.voteAverage,
                        isCurrent = part.id == id,
                    )
                },
        )
    }
    return MovieDetail(
        summary = summary,
        tagline = tagline?.takeIf { it.isNotBlank() },
        runtimeMinutes = runtime?.takeIf { it > 0 },
        releaseDate = releaseDate,
        genres = genres.map { it.toGenre() },
        cast = credits?.cast.orEmpty().sortedBy { it.order }.take(20).map { it.toCastMember() },
        directors = credits?.crew.orEmpty().filter { it.job == "Director" }.map { it.name }.distinct(),
        franchise = franchise,
        similar = similar?.results.orEmpty()
            .mapNotNull { it.toSummary(MediaType.MOVIE) }
            .filter { it.voteCount > 0 }
            .take(8),
    )
}

fun EpisodeDto.toEpisode() = Episode(
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    name = name?.takeIf { it.isNotBlank() } ?: "Episode $episodeNumber",
    overview = overview.orEmpty(),
    airDate = airDate,
    rating = voteAverage,
    voteCount = voteCount,
    stillUrl = TmdbImages.still(stillPath),
)

fun SeasonDto.toSeason() = Season(
    seasonNumber = seasonNumber,
    name = name?.takeIf { it.isNotBlank() } ?: "Season $seasonNumber",
    airDate = airDate,
    posterUrl = TmdbImages.poster(posterPath),
    episodes = episodes.sortedBy { it.episodeNumber }.map { it.toEpisode() },
)

fun TvDetailDto.toTvDetail(seasons: List<Season>): TvDetail {
    val summary = TitleSummary(
        id = id,
        mediaType = MediaType.TV,
        name = name,
        overview = overview.orEmpty(),
        posterUrl = TmdbImages.poster(posterPath),
        backdropUrl = TmdbImages.backdrop(backdropPath),
        year = firstAirDate.toYear(),
        rating = voteAverage,
        voteCount = voteCount,
        imdbId = externalIds?.imdbId?.takeIf { it.isNotBlank() },
    )
    return TvDetail(
        summary = summary,
        tagline = tagline?.takeIf { it.isNotBlank() },
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        status = status,
        episodeRuntimeMinutes = episodeRunTime.firstOrNull(),
        genres = genres.map { it.toGenre() },
        cast = credits?.cast.orEmpty().sortedBy { it.order }.take(20).map { it.toCastMember() },
        creators = createdBy.map { it.name },
        networks = networks.map { it.name },
        seasons = seasons.sortedBy { it.seasonNumber },
    )
}
