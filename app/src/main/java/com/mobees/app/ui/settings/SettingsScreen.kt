package com.mobees.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.BuildConfig
import com.mobees.app.data.ratings.RatingsProvider
import com.mobees.app.ui.components.BadgeTone
import com.mobees.app.ui.components.SettingsCard
import com.mobees.app.ui.components.StatusBadge
import com.mobees.app.ui.components.SubScreen
import com.mobees.app.ui.containerViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenStatus: () -> Unit,
) {
    val viewModel = containerViewModel { SettingsViewModel(it.settings, it.hasTmdbKey, it.hasOmdbKey) }
    val provider by viewModel.ratingsProvider.collectAsStateWithLifecycle()

    SubScreen(title = "Settings", subtitle = "Choose where ratings come from.", onBack = onBack) {
        SettingsCard(
            title = "Ratings source",
            subtitle = "Applies to detail screens and the series graph. Lists always show TMDB ratings to stay within OMDb's daily quota.",
        ) {
            ProviderOption(
                title = "IMDb ratings",
                description = "Exact IMDb scores fetched through OMDb, including every episode.",
                selected = provider == RatingsProvider.OMDB,
                enabled = viewModel.hasOmdbKey,
                disabledHint = "Add an OMDb API key (OMDB_API_KEY) to enable.",
                onSelect = { viewModel.setRatingsProvider(RatingsProvider.OMDB) },
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline)
            ProviderOption(
                title = "TMDB ratings",
                description = "Community ratings from The Movie Database. No extra requests.",
                selected = provider == RatingsProvider.TMDB,
                enabled = true,
                disabledHint = null,
                onSelect = { viewModel.setRatingsProvider(RatingsProvider.TMDB) },
            )
        }

        SettingsCard(title = "Services in use") {
            ServiceRow(
                role = "Catalogue",
                service = if (viewModel.hasTmdbKey) "TMDB" else "Demo data",
                detail = if (viewModel.hasTmdbKey) "Search, trending, artwork, episodes" else "Bundled sample titles · add TMDB_API_KEY for live data",
            )
            Spacer(Modifier.height(10.dp))
            ServiceRow(
                role = "Ratings",
                service = if (provider == RatingsProvider.OMDB) "OMDb (IMDb)" else "TMDB",
                detail = if (provider == RatingsProvider.OMDB) "Title and episode ratings from IMDb" else "Ratings shown as provided by TMDB",
            )
            Spacer(Modifier.height(14.dp))
            Button(onClick = onOpenStatus, modifier = Modifier.fillMaxWidth()) { Text("View service status") }
        }

        SettingsCard(title = "About") {
            Text(
                "Mobees ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Catalogue data by TMDB. IMDb ratings via OMDb. This app is not endorsed or certified by TMDB, OMDb or IMDb.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProviderOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    disabledHint: String?,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onSelect)
            .alpha(if (enabled) 1f else 0.5f)
            .padding(vertical = 8.dp),
    ) {
        RadioButton(selected = selected, onClick = if (enabled) onSelect else null, enabled = enabled)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!enabled && disabledHint != null) {
                Text(
                    disabledHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        if (selected) StatusBadge("Active", BadgeTone.POSITIVE)
    }
}

@Composable
private fun ServiceRow(role: String, service: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f)) {
            Text(role, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(service, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StatusBadge("Active", BadgeTone.POSITIVE)
    }
}
