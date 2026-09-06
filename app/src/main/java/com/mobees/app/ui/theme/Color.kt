package com.mobees.app.ui.theme

import androidx.compose.ui.graphics.Color

// Brand
val Gold = Color(0xFFF5C518)
val GoldDark = Color(0xFFC79A00)
val OnGold = Color(0xFF1B1400)

// Dark palette (near-black canvas with soft elevated surfaces)
val DarkBackground = Color(0xFF0F0F14)
val DarkSurface = Color(0xFF15151C)
val DarkSurfaceContainer = Color(0xFF1C1C25)
val DarkSurfaceContainerHigh = Color(0xFF24242F)
val DarkOnBackground = Color(0xFFF4F4F6)
val DarkOnSurfaceVariant = Color(0xFFA0A0AE)
val DarkOutline = Color(0xFF34343F)

// Light palette (clean white canvas with warm greys)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFF4F4F6)
val LightSurfaceContainerHigh = Color(0xFFECECF0)
val LightOnBackground = Color(0xFF15151C)
val LightOnSurfaceVariant = Color(0xFF6B6B78)
val LightOutline = Color(0xFFE2E2E8)

/** Distinct hues used to colour seasons in the trend chart and legend. */
val SeasonPalette = listOf(
    Color(0xFF4F8CFF),
    Color(0xFFFF6B6B),
    Color(0xFF2FBF71),
    Color(0xFFFFB84D),
    Color(0xFFB57BFF),
    Color(0xFF2EC4B6),
    Color(0xFFFF7AB6),
    Color(0xFFA3E635),
    Color(0xFFFF9F1C),
    Color(0xFF8AB6FF),
)

fun seasonColor(seasonNumber: Int): Color = SeasonPalette[(seasonNumber - 1).mod(SeasonPalette.size)]

val Color_Secondary_Dark = Color(0xFF8AB6FF)
val Color_Secondary_Light = Color(0xFF3B6FD8)
