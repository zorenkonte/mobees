package com.mobees.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.TitleRepository
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.ui.userMessage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val trendingMovies: List<TitleSummary> = emptyList(),
    val popularTv: List<TitleSummary> = emptyList(),
    val topRated: List<TitleSummary> = emptyList(),
    val isDemo: Boolean = false,
)

class HomeViewModel(private val repository: TitleRepository) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isDemo = repository.isDemo))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                coroutineScope {
                    val trending = async { repository.trendingMovies() }
                    val popular = async { repository.popularTv() }
                    val top = async { repository.topRated() }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            trendingMovies = trending.await(),
                            popularTv = popular.await(),
                            topRated = top.await(),
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.userMessage()) }
            }
        }
    }
}
