package com.mobees.app.di

import android.content.Context
import com.mobees.app.BuildConfig
import com.mobees.app.data.SavedTitlesStore
import com.mobees.app.data.Service
import com.mobees.app.data.ServiceStatusTracker
import com.mobees.app.data.SettingsStore
import com.mobees.app.data.TitleRepository
import com.mobees.app.data.demo.DemoRepository
import com.mobees.app.data.ratings.OmdbClient
import com.mobees.app.data.ratings.OmdbRatingsSource
import com.mobees.app.data.ratings.RatedTitleRepository
import com.mobees.app.data.ratings.defaultRatingsProvider
import com.mobees.app.data.remote.TmdbApi
import com.mobees.app.data.remote.TmdbClient
import com.mobees.app.data.remote.TmdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Hand-rolled dependency container; kept deliberately small. */
class AppContainer(private val context: Context) {

    val hasTmdbKey: Boolean = BuildConfig.TMDB_API_KEY.isNotBlank()
    val hasOmdbKey: Boolean = BuildConfig.OMDB_API_KEY.isNotBlank()
    val isDemoMode: Boolean = !hasTmdbKey

    val status: ServiceStatusTracker by lazy {
        ServiceStatusTracker(
            context,
            configured = mapOf(
                Service.TMDB to hasTmdbKey,
                Service.OMDB to hasOmdbKey,
                Service.DEMO to true,
            ),
        )
    }

    private val tmdbApi: TmdbApi? by lazy {
        if (hasTmdbKey) {
            TmdbClient.create(
                BuildConfig.TMDB_API_KEY,
                enableLogging = BuildConfig.DEBUG,
                interceptors = listOf(status.interceptor(Service.TMDB)),
            )
        } else {
            null
        }
    }

    /** Catalogue: TMDB when a key is configured, otherwise the bundled demo dataset. */
    val repository: TitleRepository by lazy {
        val api = tmdbApi
        if (api != null) {
            TmdbRepository(api)
        } else {
            DemoRepository {
                withContext(Dispatchers.IO) {
                    context.assets.open("demo_titles.json").bufferedReader().use { it.readText() }
                }
            }
        }
    }

    val omdbSource: OmdbRatingsSource? by lazy {
        if (hasOmdbKey) {
            OmdbRatingsSource(
                OmdbClient.create(
                    BuildConfig.OMDB_API_KEY,
                    enableLogging = BuildConfig.DEBUG,
                    interceptors = listOf(status.interceptor(Service.OMDB)),
                ),
            )
        } else {
            null
        }
    }

    val settings: SettingsStore by lazy { SettingsStore(context, defaultRatingsProvider(hasOmdbKey)) }

    /** Detail screens read through this so ratings follow the user's provider setting. */
    val ratedRepository: RatedTitleRepository by lazy {
        RatedTitleRepository(repository, omdbSource) { settings.current() }
    }

    val savedTitles: SavedTitlesStore by lazy { SavedTitlesStore(context) }

    /** Connectivity test used by the Status page. Returns a short human-readable result. */
    suspend fun ping(service: Service): String = when (service) {
        Service.TMDB -> {
            val api = tmdbApi ?: error("TMDB API key is not configured")
            api.configuration()
            "TMDB responded"
        }
        Service.OMDB -> {
            val source = omdbSource ?: error("OMDb API key is not configured")
            val rating = source.probe(PROBE_IMDB_ID)
            if (rating != null) "OMDb responded · Breaking Bad rates ${rating.rating} on IMDb" else "OMDb responded"
        }
        Service.DEMO -> {
            val count = repository.trendingMovies().size + repository.popularTv().size
            "$count demo titles loaded"
        }
    }

    private companion object {
        const val PROBE_IMDB_ID = "tt0903747"
    }
}
