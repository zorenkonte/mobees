package com.mobees.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.UiState
import com.mobees.app.ui.charts.ChartPoint
import com.mobees.app.ui.charts.RatingBarChart
import com.mobees.app.ui.charts.RatingLineChart
import com.mobees.app.ui.components.CastRow
import com.mobees.app.ui.components.ErrorState
import com.mobees.app.ui.components.ExpandableText
import com.mobees.app.ui.components.InfoBanner
import com.mobees.app.ui.components.LoadingState
import com.mobees.app.ui.components.SectionHeader
import com.mobees.app.ui.containerViewModel

@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    onOpenTitle: (TitleSummary) -> Unit,
) {
    val viewModel = containerViewModel(key = "movie-$movieId") {
        MovieDetailViewModel(movieId, it.ratedRepository, it.savedTitles)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()

    when (val s = state) {
        UiState.Loading -> LoadingState()
        is UiState.Error -> ErrorState(s.message, onRetry = viewModel::load)
        is UiState.Success -> MovieDetailContent(
            movie = s.data,
            notice = notice,
            isSaved = isSaved,
            onBack = onBack,
            onToggleSaved = { viewModel.toggleSaved(s.data.summary) },
            onOpenTitle = onOpenTitle,
        )
    }
}

@Composable
private fun MovieDetailContent(
    movie: MovieDetail,
    notice: String?,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onOpenTitle: (TitleSummary) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailHeader(
            summary = movie.summary,
            metaChips = listOfNotNull(
                movie.summary.year,
                formatRuntime(movie.runtimeMinutes),
                "Movie",
            ),
            genres = movie.genres,
            tagline = movie.tagline,
            isSaved = isSaved,
            onBack = onBack,
            onToggleSaved = onToggleSaved,
        )
        Column(Modifier.offset(y = (-40).dp)) {
            if (notice != null) {
                InfoBanner(notice, Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(20.dp))
            }
            if (movie.summary.overview.isNotBlank()) {
                SectionHeader("Overview")
                Spacer(Modifier.height(8.dp))
                ExpandableText(movie.summary.overview, Modifier.padding(horizontal = 20.dp))
                if (movie.directors.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Directed by ${movie.directors.joinToString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            val franchise = movie.franchise
            if (franchise != null && franchise.entries.size > 1) {
                SectionCard(
                    title = "Franchise graph",
                    subtitle = "${franchise.name} · TMDB ratings in release order",
                ) {
                    RatingLineChart(
                        points = franchise.entries.map { e ->
                            ChartPoint(label = e.title, sublabel = e.year, value = e.rating, highlighted = e.isCurrent)
                        },
                    )
                }
                Spacer(Modifier.height(24.dp))
            } else if (movie.similar.isNotEmpty()) {
                SectionCard(
                    title = "How it compares",
                    subtitle = "TMDB ratings against similar titles",
                ) {
                    val points = listOf(
                        ChartPoint(movie.summary.name, movie.summary.year, movie.tmdbRating, highlighted = true),
                    ) + movie.similar.take(6).map { ChartPoint(it.name, it.year, it.rating) }
                    RatingBarChart(points.sortedByDescending { it.value })
                }
                Spacer(Modifier.height(24.dp))
            }

            if (movie.cast.isNotEmpty()) {
                SectionHeader("Cast")
                Spacer(Modifier.height(14.dp))
                CastRow(movie.cast)
                Spacer(Modifier.height(28.dp))
            }

            if (movie.similar.isNotEmpty()) {
                SectionHeader("More like this")
                Spacer(Modifier.height(14.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                ) {
                    items(movie.similar.size) { i ->
                        val t = movie.similar[i]
                        com.mobees.app.ui.components.PosterCard(title = t, onClick = { onOpenTitle(t) }, width = 120.dp)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
