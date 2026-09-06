package com.mobees.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.userMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val filter: MediaType? = null,
    val isSearching: Boolean = false,
    val results: List<TitleSummary> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: TitleRepository) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<MediaType?>(null)

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(query.debounce(300), filter) { q, f -> q.trim() to f }
                .distinctUntilChanged()
                .collect { (q, f) -> runSearch(q, f) }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
        _state.update { it.copy(query = value, isSearching = value.isNotBlank()) }
    }

    fun onFilterChange(value: MediaType?) {
        filter.value = value
        _state.update { it.copy(filter = value) }
    }

    fun clear() = onQueryChange("")

    private suspend fun runSearch(q: String, f: MediaType?) {
        if (q.isBlank()) {
            _state.update { it.copy(isSearching = false, results = emptyList(), error = null, hasSearched = false) }
            return
        }
        _state.update { it.copy(isSearching = true, error = null) }
        try {
            val results = repository.search(q, f)
            _state.update { it.copy(isSearching = false, results = results, hasSearched = true) }
        } catch (e: Exception) {
            _state.update { it.copy(isSearching = false, error = e.userMessage(), hasSearched = true) }
        }
    }
}
