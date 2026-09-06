package com.mobees.app.data.ratings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OmdbTitleDto(
    @SerialName("Response") val response: String = "False",
    @SerialName("Error") val error: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("imdbID") val imdbId: String? = null,
    @SerialName("imdbRating") val imdbRating: String? = null,
    @SerialName("imdbVotes") val imdbVotes: String? = null,
    @SerialName("totalSeasons") val totalSeasons: String? = null,
)

@Serializable
data class OmdbEpisodeDto(
    @SerialName("Title") val title: String? = null,
    @SerialName("Released") val released: String? = null,
    @SerialName("Episode") val episode: String? = null,
    @SerialName("imdbRating") val imdbRating: String? = null,
    @SerialName("imdbID") val imdbId: String? = null,
)

@Serializable
data class OmdbSeasonDto(
    @SerialName("Response") val response: String = "False",
    @SerialName("Error") val error: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("Season") val season: String? = null,
    @SerialName("totalSeasons") val totalSeasons: String? = null,
    @SerialName("Episodes") val episodes: List<OmdbEpisodeDto> = emptyList(),
)

/** OMDb encodes missing values as the string "N/A"; these helpers normalise that. */
object OmdbParsers {
    fun rating(raw: String?): Double? =
        raw?.trim()?.takeIf { it.isNotEmpty() && it != "N/A" }?.toDoubleOrNull()?.takeIf { it > 0.0 }

    fun votes(raw: String?): Int? =
        raw?.trim()?.takeIf { it.isNotEmpty() && it != "N/A" }?.replace(",", "")?.toIntOrNull()

    fun int(raw: String?): Int? =
        raw?.trim()?.takeIf { it.isNotEmpty() && it != "N/A" }?.toIntOrNull()

    fun isSuccess(response: String): Boolean = response.equals("True", ignoreCase = true)

    fun isNotFound(error: String?): Boolean = error?.contains("not found", ignoreCase = true) == true
}
