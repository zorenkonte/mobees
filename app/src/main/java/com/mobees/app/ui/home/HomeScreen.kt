package com.mobees.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.components.DemoBanner
import com.mobees.app.ui.components.ErrorState
import com.mobees.app.ui.components.LoadingState
import com.mobees.app.ui.components.PosterCard
import com.mobees.app.ui.components.SectionHeader
import com.mobees.app.ui.containerViewModel

@Composable
fun HomeScreen(
    onOpenTitle: (TitleSummary) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatus: () -> Unit,
    contentPadding: PaddingValues,
) {
    val viewModel = containerViewModel { HomeViewModel(it.repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.isLoading -> LoadingState(Modifier.padding(contentPadding))
        state.error != null -> ErrorState(state.error!!, Modifier.padding(contentPadding), onRetry = viewModel::load)
        else -> HomeContent(state, onOpenTitle, onOpenSearch, onOpenSettings, onOpenStatus, contentPadding)
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onOpenTitle: (TitleSummary) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatus: () -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Mobees", style = MaterialTheme.typography.displaySmall)
                        Text(
                            "Browse titles, then see how every episode rates.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(20.dp))
                SearchEntry(onClick = onOpenSearch)
                if (state.isDemo) {
                    Spacer(Modifier.height(12.dp))
                    DemoBanner(onClick = onOpenStatus)
                }
            }
        }
        if (state.trendingMovies.isNotEmpty()) {
            item { Carousel("Trending movies", "This week", state.trendingMovies, onOpenTitle) }
        }
        if (state.popularTv.isNotEmpty()) {
            item { Carousel("Popular series", "Tap a show to open its series graph", state.popularTv, onOpenTitle) }
        }
        if (state.topRated.isNotEmpty()) {
            item { Carousel("Top rated", "Movies and series, highest first", state.topRated, onOpenTitle, cardWidth = 120.dp) }
        }
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(
                "Search movies and series",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Carousel(
    title: String,
    subtitle: String,
    titles: List<TitleSummary>,
    onOpenTitle: (TitleSummary) -> Unit,
    cardWidth: androidx.compose.ui.unit.Dp = 140.dp,
) {
    Column {
        SectionHeader(title = title, subtitle = subtitle)
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(titles, key = { "${it.mediaType}-${it.id}" }) { t ->
                PosterCard(title = t, onClick = { onOpenTitle(t) }, width = cardWidth)
            }
        }
    }
}
