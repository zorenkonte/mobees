package com.mobees.app.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobees.app.domain.RatingScale
import com.mobees.app.ui.theme.Gold

data class ChartPoint(
    val label: String,
    val sublabel: String?,
    val value: Double,
    val highlighted: Boolean = false,
)

/** Line chart of ratings across an ordered set of titles, e.g. a franchise in release order. */
@Composable
fun RatingLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.secondary
    val range = ratingAxisRange(points.map { it.value })

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(200.dp),
        ) {
            if (points.isEmpty()) return@Canvas
            val leftPad = 30.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 16.dp.toPx()
            val bottomPad = 34.dp.toPx()
            val plotWidth = size.width - leftPad - rightPad
            val plotHeight = size.height - topPad - bottomPad
            val minY = range.start
            val maxY = range.endInclusive
            fun yFor(v: Double) = (topPad + plotHeight * (1 - (v - minY) / (maxY - minY))).toFloat()
            val step = if (points.size > 1) plotWidth / (points.size - 1) else 0f
            fun xFor(i: Int) = leftPad + if (points.size > 1) step * i else plotWidth / 2

            var tick = minY
            while (tick <= maxY + 0.001) {
                val y = yFor(tick)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width - rightPad, y), 1f)
                val label = textMeasurer.measure(tick.toInt().toString(), TextStyle(fontSize = 10.sp, color = labelColor))
                drawText(label, topLeft = Offset(leftPad - label.size.width - 6.dp.toPx(), y - label.size.height / 2))
                tick += 1
            }

            val path = Path()
            points.forEachIndexed { i, p ->
                val x = xFor(i)
                val y = yFor(p.value)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            points.forEachIndexed { i, p ->
                val center = Offset(xFor(i), yFor(p.value))
                if (p.highlighted) {
                    drawCircle(Gold, radius = 7.dp.toPx(), center = center)
                    drawCircle(lineColor, radius = 7.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
                } else {
                    drawCircle(lineColor, radius = 4.dp.toPx(), center = center)
                }
                val value = textMeasurer.measure(
                    RatingScale.format(p.value),
                    TextStyle(fontSize = 11.sp, color = labelColor, fontWeight = if (p.highlighted) FontWeight.Bold else FontWeight.Normal),
                )
                drawText(value, topLeft = Offset(center.x - value.size.width / 2, center.y - value.size.height - 9.dp.toPx()))
                val axis = textMeasurer.measure(
                    p.sublabel ?: p.label,
                    TextStyle(fontSize = 10.sp, color = labelColor, fontWeight = if (p.highlighted) FontWeight.Bold else FontWeight.Normal),
                )
                drawText(axis, topLeft = Offset(center.x - axis.size.width / 2, size.height - axis.size.height))
            }
            drawLine(
                gridColor,
                Offset(leftPad, topPad + plotHeight),
                Offset(size.width - rightPad, topPad + plotHeight),
                1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
            )
        }
    }
}

/** Horizontal bars comparing a title's rating with related titles. */
@Composable
fun RatingBarChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        points.forEach { p ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    p.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (p.highlighted) FontWeight.Bold else FontWeight.Normal,
                    color = if (p.highlighted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(120.dp),
                )
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((p.value / 10.0).toFloat().coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (p.highlighted) Gold else ratingColor(p.value)),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    RatingScale.format(p.value),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                )
            }
        }
    }
}
