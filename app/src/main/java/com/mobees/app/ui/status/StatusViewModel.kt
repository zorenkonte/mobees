package com.mobees.app.ui.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.Service
import com.mobees.app.data.ServiceStatus
import com.mobees.app.data.ratings.RatingsProvider
import com.mobees.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PingResult(val running: Boolean = false, val success: Boolean? = null, val message: String? = null, val millis: Long? = null)

data class ServiceCard(
    val status: ServiceStatus,
    /** Roles this service currently fulfils, e.g. ["Catalogue", "Ratings"]. Empty = inactive. */
    val activeRoles: List<String>,
    val ping: PingResult,
)

class StatusViewModel(private val container: AppContainer) : ViewModel() {

    private val pings = MutableStateFlow<Map<Service, PingResult>>(emptyMap())

    val cards: StateFlow<List<ServiceCard>> = combine(
        container.status.statuses,
        container.settings.ratingsProvider,
        pings.asStateFlow(),
    ) { statuses, provider, pings ->
        Service.entries.map { service ->
            ServiceCard(
                status = statuses.getValue(service),
                activeRoles = rolesFor(service, provider),
                ping = pings[service] ?: PingResult(),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun ping(service: Service) {
        pings.update { it + (service to PingResult(running = true)) }
        viewModelScope.launch {
            val started = System.currentTimeMillis()
            val result = runCatching { container.ping(service) }
            val elapsed = System.currentTimeMillis() - started
            pings.update {
                it + (service to PingResult(
                    running = false,
                    success = result.isSuccess,
                    message = result.getOrElse { e -> e.message ?: e.javaClass.simpleName },
                    millis = elapsed,
                ))
            }
        }
    }

    private fun rolesFor(service: Service, provider: RatingsProvider): List<String> = buildList {
        when (service) {
            Service.TMDB -> {
                if (container.hasTmdbKey) add("Catalogue")
                if (provider == RatingsProvider.TMDB) add("Ratings")
            }
            Service.OMDB -> if (provider == RatingsProvider.OMDB && container.hasOmdbKey) add("Ratings")
            Service.DEMO -> if (!container.hasTmdbKey) add("Catalogue")
        }
    }
}
