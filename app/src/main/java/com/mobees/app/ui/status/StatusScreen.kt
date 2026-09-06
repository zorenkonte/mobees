package com.mobees.app.ui.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.data.Outcome
import com.mobees.app.data.Service
import com.mobees.app.ui.components.BadgeTone
import com.mobees.app.ui.components.KeyValueRow
import com.mobees.app.ui.components.SettingsCard
import com.mobees.app.ui.components.StatusBadge
import com.mobees.app.ui.components.SubScreen
import com.mobees.app.ui.containerViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun StatusScreen(onBack: () -> Unit) {
    val viewModel = containerViewModel { StatusViewModel(it) }
    val cards by viewModel.cards.collectAsStateWithLifecycle()

    SubScreen(
        title = "Service status",
        subtitle = "Which services power the app right now, and whether they respond.",
        onBack = onBack,
    ) {
        cards.forEach { card -> ServiceCardView(card, onTest = { viewModel.ping(card.status.service) }) }
    }
}

@Composable
private fun ServiceCardView(card: ServiceCard, onTest: () -> Unit) {
    val status = card.status
    val service = status.service
    val active = card.activeRoles.isNotEmpty()
    val positive = Color(0xFF3DBB63)
    val negative = Color(0xFFE5675F)

    SettingsCard(title = service.displayName, subtitle = service.role) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                !status.configured && service != Service.DEMO -> StatusBadge("Not configured", BadgeTone.WARNING)
                active -> StatusBadge("Active · ${card.activeRoles.joinToString(" + ")}", BadgeTone.POSITIVE)
                else -> StatusBadge("Inactive", BadgeTone.NEUTRAL)
            }
            if (status.lastOutcome == Outcome.ERROR) StatusBadge("Last call failed", BadgeTone.NEGATIVE)
        }
        Spacer(Modifier.height(8.dp))
        KeyValueRow(
            "API key",
            when {
                service == Service.DEMO -> "Not required"
                status.configured -> "Configured"
                else -> "Missing"
            },
        )
        if (service != Service.DEMO) {
            KeyValueRow(
                "Requests today",
                service.dailyQuota?.let { "${status.requestsToday} of $it" } ?: status.requestsToday.toString(),
            )
        }
        KeyValueRow(
            "Last call",
            status.lastCallAt?.let { at ->
                val outcome = when (status.lastOutcome) {
                    Outcome.OK -> "OK"
                    Outcome.ERROR -> "Failed"
                    null -> ""
                }
                listOfNotNull(formatTime(at), outcome.takeIf { it.isNotEmpty() }, status.lastMessage).joinToString(" · ")
            } ?: "No calls yet",
            valueColor = when (status.lastOutcome) {
                Outcome.OK -> positive
                Outcome.ERROR -> negative
                null -> null
            },
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onTest,
                enabled = !card.ping.running && (status.configured || service == Service.DEMO),
            ) {
                if (card.ping.running) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Test connection")
            }
        }
        val ping = card.ping
        if (ping.success != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                buildString {
                    append(if (ping.success) "Success" else "Failed")
                    ping.millis?.let { append(" in ${it} ms") }
                    ping.message?.let { append(" · $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (ping.success) positive else negative,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            )
        }
    }
}

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun formatTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime().format(timeFormatter)
