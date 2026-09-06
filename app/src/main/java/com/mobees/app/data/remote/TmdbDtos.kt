package com.mobees.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PagedResponse<T>(
    val page: Int = 1,
    val results: List<T> = emptyList(),
)

@Serializable
data class TmdbTitleDto(
    val id: Int,
    @SerialName("media_type") val mediaType: String? = null,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
)

@Serializable
data class GenreDto(val id: Int, val name: String)

@Serializable
data class CastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null,
    val order: Int = 0,
)

@Serializable
data class CrewDto(
    val id: Int,
    val name: String,
    val job: String? = null,
    val department: String? = null,
)

@Serializable
data class CreditsDto(
    val cast: List<CastDto> = emptyList(),
    val crew: List<CrewDto> = emptyList(),
)

@Serializable
data class ExternalIdsDto(
    @SerialName("imdb_id") val imdbId: String? = null,
)

@Serializable
data class CollectionRefDto(
    val id: Int,
    val name: String,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
)

@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String,
    val overview: String? = null,
    val tagline: String? = null,
    val runtime: Int? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("belongs_to_collection") val belongsToCollection: CollectionRefDto? = null,
    val credits: CreditsDto? = null,
    val similar: PagedResponse<TmdbTitleDto>? = null,
    @SerialName("external_ids") val externalIds: ExternalIdsDto? = null,
)

@Serializable
data class CollectionDto(
    val id: Int,
    val name: String,
    val parts: List<TmdbTitleDto> = emptyList(),
)

@Serializable
data class SeasonRefDto(
    @SerialName("season_number") val seasonNumber: Int,
    val name: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("episode_count") val episodeCount: Int = 0,
    @SerialName("poster_path") val posterPath: String? = null,
)

@Serializable
data class PersonRefDto(val id: Int, val name: String)

@Serializable
data class NetworkDto(val id: Int, val name: String)

@Serializable
data class TvDetailDto(
    val id: Int,
    val name: String,
    val overview: String? = null,
    val tagline: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("last_air_date") val lastAirDate: String? = null,
    val status: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int = 0,
    @SerialName("episode_run_time") val episodeRunTime: List<Int> = emptyList(),
    val seasons: List<SeasonRefDto> = emptyList(),
    @SerialName("created_by") val createdBy: List<PersonRefDto> = emptyList(),
    val networks: List<NetworkDto> = emptyList(),
    val credits: CreditsDto? = null,
    @SerialName("external_ids") val externalIds: ExternalIdsDto? = null,
)

@Serializable
data class EpisodeDto(
    val id: Int,
    @SerialName("episode_number") val episodeNumber: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String? = null,
    val overview: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("still_path") val stillPath: String? = null,
)

@Serializable
data class SeasonDto(
    val id: Int,
    @SerialName("season_number") val seasonNumber: Int,
    val name: String? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val episodes: List<EpisodeDto> = emptyList(),
)
