package com.mobees.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import java.io.IOException
import java.time.Instant
import java.time.ZoneId

enum class Service(val displayName: String, val role: String, val dailyQuota: Int?) {
    TMDB("TMDB", "Catalogue, images and TMDB ratings", null),
    OMDB("OMDb", "IMDb ratings for detail screens", 1_000),
    DEMO("Demo data", "Bundled sample catalogue", null),
}

enum class Outcome { OK, ERROR }

data class ServiceStatus(
    val service: Service,
    val configured: Boolean,
    val requestsToday: Int = 0,
    val lastCallAt: Long? = null,
    val lastOutcome: Outcome? = null,
    val lastMessage: String? = null,
)

data class DailyCount(val date: String, val count: Int)

/** Pure helpers behind the tracker, unit-tested separately. */
object StatusMath {
    fun today(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().toString()

    fun bump(previous: DailyCount?, today: String): DailyCount =
        if (previous == null || previous.date != today) DailyCount(today, 1) else previous.copy(count = previous.count + 1)

    fun countFor(previous: DailyCount?, today: String): Int =
        if (previous == null || previous.date != today) 0 else previous.count
}

private val Context.statusDataStore: DataStore<Preferences> by preferencesDataStore(name = "service_status")

/**
 * Records every outgoing request per service (through an OkHttp interceptor) so the Status
 * page can show request counts for today, the last result and any error message.
 */
class ServiceStatusTracker(
    private val context: Context,
    configured: Map<Service, Boolean>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val counts = HashMap<Service, DailyCount>()

    private val _statuses = MutableStateFlow(
        Service.entries.associateWith { ServiceStatus(it, configured[it] ?: false) },
    )
    val statuses: StateFlow<Map<Service, ServiceStatus>> = _statuses.asStateFlow()

    init {
        scope.launch { restore() }
    }

    fun interceptor(service: Service): Interceptor = Interceptor { chain ->
        val request = chain.request()
        try {
            val response = chain.proceed(request)
            record(
                service,
                success = response.isSuccessful,
                message = if (response.isSuccessful) "HTTP ${response.code}" else "HTTP ${response.code} ${response.message}".trim(),
            )
            response
        } catch (e: IOException) {
            record(service, success = false, message = e.message ?: e.javaClass.simpleName)
            throw e
        }
    }

    fun record(service: Service, success: Boolean, message: String?) {
        val now = clock()
        val today = StatusMath.today(now)
        val count = synchronized(counts) {
            StatusMath.bump(counts[service], today).also { counts[service] = it }
        }
        _statuses.update { map ->
            val current = map.getValue(service)
            map + (service to current.copy(
                requestsToday = count.count,
                lastCallAt = now,
                lastOutcome = if (success) Outcome.OK else Outcome.ERROR,
                lastMessage = message,
            ))
        }
        scope.launch { persist(service, count, now, success, message) }
    }

    private suspend fun restore() {
        val prefs = context.statusDataStore.data.first()
        val today = StatusMath.today(clock())
        Service.entries.forEach { service ->
            val date = prefs[stringPreferencesKey("date_${service.name}")]
            val stored = prefs[intPreferencesKey("count_${service.name}")]
            val daily = if (date != null && stored != null) DailyCount(date, stored) else null
            synchronized(counts) { if (daily != null && !counts.containsKey(service)) counts[service] = daily }
            val lastAt = prefs[longPreferencesKey("last_at_${service.name}")]
            val lastOk = prefs[booleanPreferencesKey("last_ok_${service.name}")]
            val lastMsg = prefs[stringPreferencesKey("last_msg_${service.name}")]
            _statuses.update { map ->
                val current = map.getValue(service)
                if (current.lastCallAt != null) return@update map // a live call already happened
                map + (service to current.copy(
                    requestsToday = StatusMath.countFor(daily, today),
                    lastCallAt = lastAt,
                    lastOutcome = lastOk?.let { if (it) Outcome.OK else Outcome.ERROR },
                    lastMessage = lastMsg,
                ))
            }
        }
    }

    private suspend fun persist(service: Service, count: DailyCount, at: Long, success: Boolean, message: String?) {
        runCatching {
            context.statusDataStore.edit { prefs ->
                prefs[stringPreferencesKey("date_${service.name}")] = count.date
                prefs[intPreferencesKey("count_${service.name}")] = count.count
                prefs[longPreferencesKey("last_at_${service.name}")] = at
                prefs[booleanPreferencesKey("last_ok_${service.name}")] = success
                if (message != null) prefs[stringPreferencesKey("last_msg_${service.name}")] = message
            }
        }
    }
}
