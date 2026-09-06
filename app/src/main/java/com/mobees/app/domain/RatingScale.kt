package com.mobees.app.domain

import kotlin.math.roundToInt

/**
 * Maps a 0–10 rating to a colour on a red → amber → green scale. Colours are returned as
 * packed ARGB longs so this stays free of Android/Compose dependencies and unit-testable.
 */
object RatingScale {

    const val UNRATED_COLOR: Long = 0xFF3A3A44

    private data class Stop(val rating: Double, val color: Long)

    private val stops = listOf(
        Stop(0.0, 0xFFB3261E),
        Stop(5.5, 0xFFD9552B),
        Stop(6.5, 0xFFE9A23B),
        Stop(7.5, 0xFFB9C63C),
        Stop(8.5, 0xFF4CAF50),
        Stop(10.0, 0xFF1E8E3E),
    )

    fun colorFor(rating: Double): Long {
        if (rating <= 0.0) return UNRATED_COLOR
        val r = rating.coerceIn(0.0, 10.0)
        val upperIndex = stops.indexOfFirst { it.rating >= r }
        if (upperIndex <= 0) return stops.first().color
        val lower = stops[upperIndex - 1]
        val upper = stops[upperIndex]
        val t = ((r - lower.rating) / (upper.rating - lower.rating)).coerceIn(0.0, 1.0)
        return lerp(lower.color, upper.color, t)
    }

    fun label(rating: Double): String = when {
        rating <= 0.0 -> "Unrated"
        rating >= 8.5 -> "Outstanding"
        rating >= 7.5 -> "Great"
        rating >= 6.5 -> "Good"
        rating >= 5.5 -> "Average"
        else -> "Poor"
    }

    fun format(rating: Double): String = if (rating <= 0.0) "–" else "%.1f".format(rating)

    private fun lerp(a: Long, b: Long, t: Double): Long {
        fun channel(shift: Int): Long {
            val ca = (a shr shift) and 0xFF
            val cb = (b shr shift) and 0xFF
            return (ca + (cb - ca) * t).roundToInt().toLong().coerceIn(0, 255)
        }
        return (0xFFL shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }
}
