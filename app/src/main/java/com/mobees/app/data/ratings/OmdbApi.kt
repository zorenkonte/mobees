package com.mobees.app.data.ratings

import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {
    @GET(".")
    suspend fun title(@Query("i") imdbId: String): OmdbTitleDto

    @GET(".")
    suspend fun season(@Query("i") imdbId: String, @Query("Season") season: Int): OmdbSeasonDto
}
