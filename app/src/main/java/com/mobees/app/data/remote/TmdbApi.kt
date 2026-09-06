package com.mobees.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("trending/movie/week")
    suspend fun trendingMovies(): PagedResponse<TmdbTitleDto>

    @GET("tv/popular")
    suspend fun popularTv(): PagedResponse<TmdbTitleDto>

    @GET("movie/top_rated")
    suspend fun topRatedMovies(): PagedResponse<TmdbTitleDto>

    @GET("tv/top_rated")
    suspend fun topRatedTv(): PagedResponse<TmdbTitleDto>

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponse<TmdbTitleDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponse<TmdbTitleDto>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("include_adult") includeAdult: Boolean = false,
    ): PagedResponse<TmdbTitleDto>

    @GET("movie/{id}")
    suspend fun movie(
        @Path("id") id: Int,
        @Query("append_to_response") append: String = "credits,similar",
    ): MovieDetailDto

    @GET("collection/{id}")
    suspend fun collection(@Path("id") id: Int): CollectionDto

    @GET("tv/{id}")
    suspend fun tv(
        @Path("id") id: Int,
        @Query("append_to_response") append: String = "credits",
    ): TvDetailDto

    @GET("tv/{id}/season/{season}")
    suspend fun season(
        @Path("id") id: Int,
        @Path("season") season: Int,
    ): SeasonDto
}
