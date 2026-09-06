package com.mobees.app.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.Season
import com.mobees.app.domain.RatingScale

/**
 * SeriesGraph-style grid: one row per season, one column per episode number, each cell
 * tinted by its rating. Season labels stay fixed while the grid scrolls horizontally.
 */
@Composable
fun SeriesHeatmap(
    seasons: List<Season>,
    selected: Episode?,
    onSelect: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxEpisodes = seasons.maxOfOrNull { s -> s.episodes.maxOfOrNull { it.episodeNumber } ?: 0 } ?: 0
    val cell = 40.dp
    val gap = 4.dp
    val scroll = rememberScrollState()
    val labelWidth = 36.dp

    Column(modifier) {
        Row {
            // Fixed season labels
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(20.dp + gap))
                seasons.forEach { season ->
                    Box(Modifier.size(labelWidth, cell), contentAlignment = Alignment.Center) {
                        Text(
                            "S${season.seasonNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(gap))
                }
            }
            Spacer(Modifier.width(gap))
            // Scrollable grid
            Column(Modifier.horizontalScroll(scroll)) {
                Row {
                    for (ep in 1..maxEpisodes) {
                        Box(Modifier.size(cell, 20.dp), contentAlignment = Alignment.Center) {
                            Text(
                                ep.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(gap))
                    }
                }
                Spacer(Modifier.height(gap))
                seasons.forEach { season ->
                    val byNumber = season.episodes.associateBy { it.episodeNumber }
                    Row {
                        for (ep in 1..maxEpisodes) {
                            val episode = byNumber[ep]
                            if (episode == null) {
                                Spacer(Modifier.size(cell))
                            } else {
                                HeatCell(
                                    episode = episode,
                                    isSelected = selected?.seasonNumber == episode.seasonNumber &&
                                        selected.episodeNumber == episode.episodeNumber,
                                    size = cell,
                                    onClick = { onSelect(episode) },
                                )
                            }
                            Spacer(Modifier.width(gap))
                        }
                    }
                    Spacer(Modifier.height(gap))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HeatLegend()
    }
}

@Composable
private fun HeatCell(
    episode: Episode,
    isSelected: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val color = ratingColor(if (episode.isRated) episode.rating else 0.0)
    val description = "${episode.code} ${episode.name}, rated ${RatingScale.format(episode.rating)}"
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground, shape) else Modifier,
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (episode.isRated) RatingScale.format(episode.rating) else "–",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeatLegend() {
    val stops = listOf(4.0, 5.5, 6.5, 7.5, 8.5, 10.0)
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(stops.map { ratingColor(it) })),
        )
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Poor", "Average", "Good", "Great", "Outstanding").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
