package com.mobees.app.data.ratings

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

data class ImdbRating(val rating: Double, val votes: Int?)

class OmdbException(message: String) : Exception(message)

/** Supplies IMDb ratings for a title and for the episodes of one season. */
interface ImdbRatingsSource {
    /** Returns null when IMDb has no rating for the title. */
    suspend fun title(imdbId: String): ImdbRating?

    /** Episode number → IMDb rating. Empty when the season is unknown or unrated. */
    suspend fun season(imdbId: String, season: Int): Map<Int, Double>
}

class OmdbRatingsSource(private val api: OmdbApi) : ImdbRatingsSource {

    private val mutex = Mutex()
    private val titleCache = HashMap<String, ImdbRating?>()
    private val seasonCache = HashMap<String, Map<Int, Double>>()

    override suspend fun title(imdbId: String): ImdbRating? {
        mutex.withLock { if (titleCache.containsKey(imdbId)) return titleCache[imdbId] }
        val dto = call { api.title(imdbId) }
        val result = when {
            OmdbParsers.isSuccess(dto.response) -> OmdbParsers.rating(dto.imdbRating)?.let { ImdbRating(it, OmdbParsers.votes(dto.imdbVotes)) }
            OmdbParsers.isNotFound(dto.error) -> null
            else -> throw OmdbException(dto.error ?: "Unknown OMDb error")
        }
        mutex.withLock { titleCache[imdbId] = result }
        return result
    }

    /** Uncached variant used by the Status page's connection test. */
    suspend fun probe(imdbId: String): ImdbRating? {
        val dto = call { api.title(imdbId) }
        if (!OmdbParsers.isSuccess(dto.response)) throw OmdbException(dto.error ?: "Unknown OMDb error")
        return OmdbParsers.rating(dto.imdbRating)?.let { ImdbRating(it, OmdbParsers.votes(dto.imdbVotes)) }
    }

    override suspend fun season(imdbId: String, season: Int): Map<Int, Double> {
        val key = "$imdbId/$season"
        mutex.withLock { seasonCache[key]?.let { return it } }
        val dto = call { api.season(imdbId, season) }
        val result = when {
            OmdbParsers.isSuccess(dto.response) -> dto.episodes.mapNotNull { ep ->
                val number = OmdbParsers.int(ep.episode) ?: return@mapNotNull null
                val rating = OmdbParsers.rating(ep.imdbRating) ?: return@mapNotNull null
                number to rating
            }.toMap()
            OmdbParsers.isNotFound(dto.error) -> emptyMap()
            else -> throw OmdbException(dto.error ?: "Unknown OMDb error")
        }
        mutex.withLock { seasonCache[key] = result }
        return result
    }

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: HttpException) {
        val body = e.response()?.errorBody()?.string()
        val message = body?.let { runCatching { OmdbClient.json.decodeFromString(OmdbTitleDto.serializer(), it).error }.getOrNull() }
        throw OmdbException(message ?: "OMDb returned HTTP ${e.code()}")
    }
}
