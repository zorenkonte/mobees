package com.mobees.app.ui.charts

import androidx.compose.ui.graphics.Color
import com.mobees.app.domain.RatingScale

fun ratingColor(rating: Double): Color = Color(RatingScale.colorFor(rating).toULong() shl 32)

/** Y-axis range used by the rating charts: a little headroom below the lowest value, capped at 10. */
fun ratingAxisRange(values: List<Double>): ClosedFloatingPointRange<Double> {
    val rated = values.filter { it > 0 }
    if (rated.isEmpty()) return 0.0..10.0
    val min = (rated.min() - 1.0).coerceAtLeast(0.0)
    val floor = kotlin.math.floor(min)
    return floor..10.0
}
