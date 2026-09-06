package com.mobees.app.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.Season
import com.mobees.app.ui.theme.seasonColor

/**
 * Line chart of every rated episode in broadcast order. Points are coloured by season, a
 * dashed line marks the series average, and tapping selects the nearest episode.
 */
@Composable
fun EpisodeTrendChart(
    seasons: List<Season>,
    average: Double,
    selected: Episode?,
    onSelect: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    highlightSeason: Int? = null,
) {
    val episodes = remember(seasons) { seasons.flatMap { it.episodes }.filter { it.isRated } }
    val range = remember(episodes) { ratingAxisRange(episodes.map { it.rating }) }
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val averageColor = MaterialTheme.colorScheme.onSurfaceVariant
    val selectionColor = MaterialTheme.colorScheme.onBackground
    val density = LocalDensity.current

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .pointerInput(episodes) {
                    detectTapGestures { tap ->
                        if (episodes.isEmpty()) return@detectTapGestures
                        val leftPad = with(density) { 30.dp.toPx() }
                        val rightPad = with(density) { 8.dp.toPx() }
                        val plotWidth = size.width - leftPad - rightPad
                        val step = if (episodes.size > 1) plotWidth / (episodes.size - 1) else 0f
                        val index = if (step == 0f) 0 else ((tap.x - leftPad) / step).let { Math.round(it) }.coerceIn(0, episodes.lastIndex)
                        onSelect(episodes[index])
                    }
                },
        ) {
            if (episodes.isEmpty()) return@Canvas
            val leftPad = 30.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 10.dp.toPx()
            val bottomPad = 26.dp.toPx()
            val plotWidth = size.width - leftPad - rightPad
            val plotHeight = size.height - topPad - bottomPad
            val minY = range.start
            val maxY = range.endInclusive
            fun yFor(v: Double) = (topPad + plotHeight * (1 - (v - minY) / (maxY - minY))).toFloat()
            val step = if (episodes.size > 1) plotWidth / (episodes.size - 1) else 0f
            fun xFor(i: Int) = leftPad + step * i

            // Horizontal grid lines with rating labels
            val gridStep = if (maxY - minY > 5) 2 else 1
            var tick = minY
            while (tick <= maxY + 0.001) {
                val y = yFor(tick)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width - rightPad, y), strokeWidth = 1f)
                val label = textMeasurer.measure(
                    text = tick.toInt().toString(),
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
                drawText(label, topLeft = Offset(leftPad - label.size.width - 6.dp.toPx(), y - label.size.height / 2))
                tick += gridStep
            }

            // Season boundaries + labels along the bottom
            var index = 0
            seasons.forEach { season ->
                val rated = season.episodes.filter { it.isRated }
                if (rated.isEmpty()) return@forEach
                val startX = xFor(index)
                val endX = xFor(index + rated.size - 1)
                if (index > 0) {
                    drawLine(
                        gridColor,
                        Offset(startX - step / 2, topPad),
                        Offset(startX - step / 2, topPad + plotHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    )
                }
                val label = textMeasurer.measure(
                    text = "S${season.seasonNumber}",
                    style = TextStyle(fontSize = 10.sp, color = seasonColor(season.seasonNumber)),
                )
                if (endX - startX + step >= label.size.width || seasons.size <= 12) {
                    drawText(label, topLeft = Offset((startX + endX) / 2 - label.size.width / 2, size.height - label.size.height))
                }
                index += rated.size
            }

            // Series average
            if (average > 0) {
                val y = yFor(average)
                drawLine(
                    averageColor,
                    Offset(leftPad, y),
                    Offset(size.width - rightPad, y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                )
            }

            // One path per season so colours change at boundaries
            index = 0
            seasons.forEach { season ->
                val rated = season.episodes.filter { it.isRated }
                if (rated.isEmpty()) return@forEach
                val dimmed = highlightSeason != null && highlightSeason != season.seasonNumber
                val color = seasonColor(season.seasonNumber).copy(alpha = if (dimmed) 0.25f else 1f)
                val path = Path()
                rated.forEachIndexed { i, ep ->
                    val x = xFor(index + i)
                    val y = yFor(ep.rating)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                rated.forEachIndexed { i, ep ->
                    val center = Offset(xFor(index + i), yFor(ep.rating))
                    drawCircle(color, radius = 3.5.dp.toPx(), center = center)
                    if (selected != null && selected.seasonNumber == ep.seasonNumber && selected.episodeNumber == ep.episodeNumber) {
                        drawCircle(selectionColor, radius = 7.dp.toPx(), center = center, style = Stroke(width = 2.dp.toPx()))
                    }
                }
                index += rated.size
            }
        }
    }
}
