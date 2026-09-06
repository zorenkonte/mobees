package com.mobees.app.data.ratings

/** Which service supplies the ratings shown on detail screens. */
enum class RatingsProvider(val displayName: String) {
    TMDB("TMDB ratings"),
    OMDB("IMDb ratings via OMDb"),
}

fun defaultRatingsProvider(hasOmdbKey: Boolean): RatingsProvider =
    if (hasOmdbKey) RatingsProvider.OMDB else RatingsProvider.TMDB
