package com.mobees.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.TvDetail
import com.mobees.app.domain.RatingScale
import com.mobees.app.domain.SeriesStats
import com.mobees.app.ui.UiState
import com.mobees.app.ui.charts.EpisodeTrendChart
import com.mobees.app.ui.charts.SeriesHeatmap
import com.mobees.app.ui.charts.ratingColor
import com.mobees.app.ui.components.CastRow
import com.mobees.app.ui.components.ErrorState
import com.mobees.app.ui.components.ExpandableText
import com.mobees.app.ui.components.LoadingState
import com.mobees.app.ui.components.SectionHeader
import com.mobees.app.ui.containerViewModel
import com.mobees.app.ui.theme.seasonColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvDetailScreen(
    tvId: Int,
    onBack: () -> Unit,
) {
    val viewModel = containerViewModel(key = "tv-$tvId") {
        TvDetailViewModel(tvId, it.repository, it.savedTitles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val selected by viewModel.selectedEpisode.collectAsStateWithLifecycle()
    val highlightSeason by viewModel.highlightSeason.collectAsStateWithLifecycle()

    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load)
        is UiState.Success -> {
            TvDetailContent(
                show = s.data,
                isSaved = isSaved,
                selected = selected,
                highlightSeason = highlightSeason,
                onBack = onBack,
                onToggleSaved = { viewModel.toggleSaved(s.data.summary) },
                onSelectEpisode = viewModel::selectEpisode,
                onToggleSeason = viewModel::toggleSeasonHighlight,
            )
            val episode = selected
            if (episode != null) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { viewModel.selectEpisode(null) },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    EpisodeSheet(episode = episode, seasons = s.data.seasons)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TvDetailContent(
    show: TvDetail,
    isSaved: Boolean,
    selected: Episode?,
    highlightSeason: Int?,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onSelectEpisode: (Episode?) -> Unit,
    onToggleSeason: (Int) -> Unit,
) {
    val overview = remember(show) { SeriesStats.overview(show.seasons) }
    val years = listOfNotNull(show.summary.year, show.lastAirDate?.take(4)?.takeIf { show.status == "Ended" })
        .distinct().joinToString("–")

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHeader(
            summary = show.summary,
            metaChips = listOfNotNull(
                years.takeIf { it.isNotBlank() },
                "${show.seasons.size} ${if (show.seasons.size == 1) "season" else "seasons"}",
                "${overview.totalEpisodes} episodes",
                formatRuntime(show.episodeRuntimeMinutes)?.let { "$it / ep" },
                show.status,
            ),
            genres = show.genres,
            tagline = show.tagline,
            isSaved = isSaved,
            onBack = onBack,
            onToggleSaved = onToggleSaved,
        )
        Column(Modifier.offset(y = (-40).dp)) {
            if (show.seasons.isNotEmpty()) {
                SectionCard(
                    title = "Series graph",
                    subtitle = "Every episode's rating, season by season. Tap a cell for details.",
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile(
                            "Series average",
                            RatingScale.format(overview.average),
                            Modifier.weight(1f),
                            accent = ratingColor(overview.average),
                        )
                        StatTile(
                            "Best",
                            overview.best?.let { "${RatingScale.format(it.rating)} · ${it.code}" } ?: "–",
                            Modifier.weight(1f),
                        )
                        StatTile(
                            "Worst",
                            overview.worst?.let { "${RatingScale.format(it.rating)} · ${it.code}" } ?: "–",
                            Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    SeriesHeatmap(
                        seasons = show.seasons,
                        selected = selected,
                        onSelect = { onSelectEpisode(it) },
                    )
                }
                Spacer(Modifier.height(20.dp))
                SectionCard(
                    title = "Rating trend",
                    subtitle = "Broadcast order · dashed line is the series average. Tap a season to focus it.",
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        overview.seasons.forEach { season ->
                            val color = seasonColor(season.seasonNumber)
                            FilterChip(
                                selected = highlightSeason == season.seasonNumber,
                                onClick = { onToggleSeason(season.seasonNumber) },
                                label = { Text("S${season.seasonNumber} · ${RatingScale.format(season.average)}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.25f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = highlightSeason == season.seasonNumber,
                                    borderColor = color.copy(alpha = 0.6f),
                                    selectedBorderColor = color,
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    EpisodeTrendChart(
                        seasons = show.seasons,
                        average = overview.average,
                        selected = selected,
                        onSelect = { onSelectEpisode(it) },
                        highlightSeason = highlightSeason,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            if (show.summary.overview.isNotBlank()) {
                SectionHeader("Overview")
                Spacer(Modifier.height(8.dp))
                ExpandableText(show.summary.overview, Modifier.padding(horizontal = 20.dp))
                val credits = buildList {
                    if (show.creators.isNotEmpty()) add("Created by ${show.creators.joinToString()}")
                    if (show.networks.isNotEmpty()) add(show.networks.joinToString())
                }
                if (credits.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        credits.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            if (show.cast.isNotEmpty()) {
                SectionHeader("Cast")
                Spacer(Modifier.height(14.dp))
                CastRow(show.cast)
            }
            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun EpisodeSheet(episode: Episode, seasons: List<com.mobees.app.data.model.Season>) {
    val season = seasons.firstOrNull { it.seasonNumber == episode.seasonNumber }
    val seasonAverage = season?.let { SeriesStats.average(it.episodes) } ?: 0.0
    Column(
        Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            "${episode.code} · ${season?.name ?: "Season ${episode.seasonNumber}"}",
            style = MaterialTheme.typography.labelLarge,
            color = seasonColor(episode.seasonNumber),
        )
        Spacer(Modifier.height(4.dp))
        Text(episode.name, style = MaterialTheme.typography.headlineSmall)
        if (episode.airDate != null) {
            Text(
                "Aired ${episode.airDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                "Episode rating",
                RatingScale.format(episode.rating),
                Modifier.weight(1f),
                accent = ratingColor(episode.rating),
            )
            StatTile("Votes", formatVotes(episode.voteCount), Modifier.weight(1f))
            StatTile("Season avg", RatingScale.format(seasonAverage), Modifier.weight(1f))
        }
        if (episode.isRated && seasonAverage > 0) {
            Spacer(Modifier.height(10.dp))
            val delta = episode.rating - seasonAverage
            Text(
                when {
                    delta > 0.05 -> "%.1f above the season average".format(delta)
                    delta < -0.05 -> "%.1f below the season average".format(-delta)
                    else -> "Right on the season average"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (episode.overview.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text(episode.overview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
