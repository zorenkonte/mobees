package com.mobees.app.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.SavedTitlesStore
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.components.EmptyState
import com.mobees.app.ui.components.TitleRow
import com.mobees.app.ui.containerViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SavedViewModel(store: SavedTitlesStore) : ViewModel() {
    val saved: StateFlow<List<TitleSummary>> =
        store.savedTitles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun SavedScreen(
    onOpenTitle: (TitleSummary) -> Unit,
    contentPadding: PaddingValues,
) {
    val viewModel = containerViewModel { SavedViewModel(it.savedTitles) }
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(12.dp))
        Text("Saved", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 20.dp))
        Text(
            if (saved.isEmpty()) "Your watchlist lives here." else "${saved.size} ${if (saved.size == 1) "title" else "titles"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))
        if (saved.isEmpty()) {
            EmptyState(
                title = "Nothing saved yet",
                message = "Tap the bookmark on any movie or series to keep it here.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(saved, key = { "${it.mediaType}-${it.id}" }) { title ->
                    TitleRow(title = title, onClick = { onOpenTitle(title) })
                }
            }
        }
    }
}
