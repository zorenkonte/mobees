package com.mobees.app.data

import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.data.model.TvDetail

interface TitleRepository {
    /** True when the app is serving bundled sample data instead of live TMDB data. */
    val isDemo: Boolean

    suspend fun trendingMovies(): List<TitleSummary>
    suspend fun popularTv(): List<TitleSummary>
    suspend fun topRated(): List<TitleSummary>
    suspend fun search(query: String, filter: MediaType? = null): List<TitleSummary>
    suspend fun movie(id: Int): MovieDetail
    suspend fun tv(id: Int): TvDetail
}
