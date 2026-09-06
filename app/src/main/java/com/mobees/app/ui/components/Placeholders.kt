package com.mobees.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.abs

/** Deterministic two-tone gradient derived from the title, used when no artwork exists. */
fun placeholderBrush(seed: String): Brush {
    val h = abs(seed.hashCode())
    val hue1 = (h % 360).toFloat()
    val hue2 = ((h / 7) % 360).toFloat()
    return Brush.linearGradient(
        listOf(
            Color.hsl(hue1, 0.45f, 0.30f),
            Color.hsl(hue2, 0.55f, 0.18f),
        ),
    )
}

@Composable
fun ArtworkPlaceholder(
    title: String,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Box(
        modifier = modifier.background(placeholderBrush(title)),
        contentAlignment = Alignment.Center,
    ) {
        if (showTitle) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

/** Loads remote artwork through Coil, falling back to a generated placeholder. */
@Composable
fun Artwork(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    contentScale: ContentScale = ContentScale.Crop,
    showTitleOnPlaceholder: Boolean = true,
) {
    Box(modifier = modifier.clip(shape)) {
        ArtworkPlaceholder(title = title, modifier = Modifier.fillMaxSize(), showTitle = showTitleOnPlaceholder)
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
