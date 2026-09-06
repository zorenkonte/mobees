package com.mobees.app.data.demo

import kotlinx.serialization.Serializable

@Serializable
data class DemoDataset(
    val movies: List<DemoMovie> = emptyList(),
    val shows: List<DemoShow> = emptyList(),
)

@Serializable
data class DemoCast(val name: String, val character: String)

@Serializable
data class DemoFranchiseEntry(val id: Int, val title: String, val year: String, val rating: Double)

@Serializable
data class DemoFranchise(val name: String, val entries: List<DemoFranchiseEntry>)

@Serializable
data class DemoMovie(
    val id: Int,
    val title: String,
    val releaseDate: String,
    val rating: Double,
    val voteCount: Int,
    val runtime: Int,
    val tagline: String? = null,
    val overview: String,
    val genres: List<String> = emptyList(),
    val directors: List<String> = emptyList(),
    val cast: List<DemoCast> = emptyList(),
    val franchise: DemoFranchise? = null,
    val similarIds: List<Int> = emptyList(),
    val trending: Boolean = false,
)

@Serializable
data class DemoEpisode(
    val number: Int,
    val name: String,
    val airDate: String? = null,
    val rating: Double,
    val voteCount: Int,
    val overview: String = "",
)

@Serializable
data class DemoSeason(
    val number: Int,
    val name: String? = null,
    val airDate: String? = null,
    val episodes: List<DemoEpisode> = emptyList(),
)

@Serializable
data class DemoShow(
    val id: Int,
    val name: String,
    val firstAirDate: String,
    val lastAirDate: String? = null,
    val status: String? = null,
    val rating: Double,
    val voteCount: Int,
    val tagline: String? = null,
    val overview: String,
    val genres: List<String> = emptyList(),
    val creators: List<String> = emptyList(),
    val networks: List<String> = emptyList(),
    val cast: List<DemoCast> = emptyList(),
    val episodeRuntime: Int? = null,
    val seasons: List<DemoSeason> = emptyList(),
    val popular: Boolean = false,
)
