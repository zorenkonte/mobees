package com.mobees.app.di

import android.content.Context
import com.mobees.app.BuildConfig
import com.mobees.app.data.SavedTitlesStore
import com.mobees.app.data.TitleRepository
import com.mobees.app.data.demo.DemoRepository
import com.mobees.app.data.remote.TmdbClient
import com.mobees.app.data.remote.TmdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Hand-rolled dependency container; kept deliberately small. */
class AppContainer(private val context: Context) {

    val isDemoMode: Boolean = BuildConfig.TMDB_API_KEY.isBlank()

    val repository: TitleRepository by lazy {
        if (isDemoMode) {
            DemoRepository {
                withContext(Dispatchers.IO) {
                    context.assets.open("demo_titles.json").bufferedReader().use { it.readText() }
                }
            }
        } else {
            TmdbRepository(TmdbClient.create(BuildConfig.TMDB_API_KEY, enableLogging = BuildConfig.DEBUG))
        }
    }

    val savedTitles: SavedTitlesStore by lazy { SavedTitlesStore(context) }
}
