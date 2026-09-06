package com.mobees.app.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.components.EmptyState
import com.mobees.app.ui.components.ErrorState
import com.mobees.app.ui.components.TitleRow
import com.mobees.app.ui.containerViewModel

@Composable
fun SearchScreen(
    onOpenTitle: (TitleSummary) -> Unit,
    contentPadding: PaddingValues,
) {
    val viewModel = containerViewModel { SearchViewModel(it.repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            "Search",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(14.dp))
        TextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            singleLine = true,
            placeholder = { Text("Titles, e.g. Breaking Bad") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = viewModel::clear) { Icon(Icons.Rounded.Close, contentDescription = "Clear") }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            shape = MaterialTheme.shapes.extraLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = state.filter == null, onClick = { viewModel.onFilterChange(null) }, label = { Text("All") })
            FilterChip(selected = state.filter == MediaType.MOVIE, onClick = { viewModel.onFilterChange(MediaType.MOVIE) }, label = { Text("Movies") })
            FilterChip(selected = state.filter == MediaType.TV, onClick = { viewModel.onFilterChange(MediaType.TV) }, label = { Text("Series") })
        }
        Spacer(Modifier.height(8.dp))
        if (state.isSearching) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 20.dp))
        } else {
            Spacer(Modifier.height(4.dp))
        }

        when {
            state.error != null -> ErrorState(state.error!!)
            state.query.isBlank() -> EmptyState(
                title = "Find something to watch",
                message = "Search by title. Open any series to see its ratings mapped season by season.",
            )
            state.hasSearched && state.results.isEmpty() && !state.isSearching -> EmptyState(
                title = "No matches",
                message = "Nothing found for \"${state.query}\". Try a different spelling or filter.",
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.results, key = { "${it.mediaType}-${it.id}" }) { title ->
                    TitleRow(title = title, onClick = { onOpenTitle(title) })
                }
            }
        }
    }
}
