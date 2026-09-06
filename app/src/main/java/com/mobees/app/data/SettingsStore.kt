package com.mobees.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobees.app.data.ratings.RatingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** User preferences. Currently just the ratings provider. */
class SettingsStore(
    private val context: Context,
    private val defaultProvider: RatingsProvider,
) {
    private val providerKey = stringPreferencesKey("ratings_provider")

    val ratingsProvider: Flow<RatingsProvider> = context.settingsDataStore.data.map { prefs ->
        prefs[providerKey]?.let { raw -> RatingsProvider.entries.firstOrNull { it.name == raw } } ?: defaultProvider
    }

    suspend fun current(): RatingsProvider = ratingsProvider.first()

    suspend fun setRatingsProvider(provider: RatingsProvider) {
        context.settingsDataStore.edit { it[providerKey] = provider.name }
    }
}
