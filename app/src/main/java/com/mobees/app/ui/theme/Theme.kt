package com.mobees.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = Gold,
    onPrimary = OnGold,
    primaryContainer = GoldDark,
    onPrimaryContainer = OnGold,
    secondary = Color_Secondary_Dark,
    onSecondary = DarkOnBackground,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceContainerHigh,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerLow = DarkSurface,
    surfaceContainerLowest = DarkBackground,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
)

private val LightColors = lightColorScheme(
    primary = GoldDark,
    onPrimary = OnGold,
    primaryContainer = Gold,
    onPrimaryContainer = OnGold,
    secondary = Color_Secondary_Light,
    onSecondary = LightBackground,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceContainerHigh,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerLow = LightSurfaceContainer,
    surfaceContainerLowest = LightBackground,
    outline = LightOutline,
    outlineVariant = LightOutline,
)

@Composable
fun MobeesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MobeesTypography,
        shapes = MobeesShapes,
        content = content,
    )
}
