package com.mobees.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.TitleSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.savedTitlesDataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_titles")

@Serializable
private data class SavedTitle(
    val id: Int,
    val mediaType: MediaType,
    val name: String,
    val overview: String = "",
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val year: String? = null,
    val rating: Double = 0.0,
    val voteCount: Int = 0,
    val imdbId: String? = null,
) {
    fun toSummary() = TitleSummary(id, mediaType, name, overview, posterUrl, backdropUrl, year, rating, voteCount, imdbId)

    companion object {
        fun from(s: TitleSummary) =
            SavedTitle(s.id, s.mediaType, s.name, s.overview, s.posterUrl, s.backdropUrl, s.year, s.rating, s.voteCount, s.imdbId)
    }
}

/** Persists the user's saved titles as a JSON list in Preferences DataStore. */
class SavedTitlesStore(private val context: Context) {

    private val key = stringPreferencesKey("saved_titles_json")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(SavedTitle.serializer())

    val savedTitles: Flow<List<TitleSummary>> = context.savedTitlesDataStore.data.map { prefs ->
        decode(prefs[key]).map { it.toSummary() }
    }

    fun isSaved(id: Int, mediaType: MediaType): Flow<Boolean> =
        savedTitles.map { list -> list.any { it.id == id && it.mediaType == mediaType } }

    suspend fun toggle(title: TitleSummary) {
        context.savedTitlesDataStore.edit { prefs ->
            val current = decode(prefs[key])
            val exists = current.any { it.id == title.id && it.mediaType == title.mediaType }
            val updated = if (exists) {
                current.filterNot { it.id == title.id && it.mediaType == title.mediaType }
            } else {
                listOf(SavedTitle.from(title)) + current
            }
            prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    private fun decode(raw: String?): List<SavedTitle> =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() } ?: emptyList()
}
