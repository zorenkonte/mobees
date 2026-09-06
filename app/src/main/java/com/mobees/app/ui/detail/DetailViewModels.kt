package com.mobees.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.SavedTitlesStore
import com.mobees.app.data.model.Episode
import com.mobees.app.data.model.MediaType
import com.mobees.app.data.model.MovieDetail
import com.mobees.app.data.model.TitleSummary
import com.mobees.app.data.model.TvDetail
import com.mobees.app.data.ratings.RatedTitleRepository
import com.mobees.app.ui.UiState
import com.mobees.app.ui.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val id: Int,
    private val repository: RatedTitleRepository,
    private val saved: SavedTitlesStore,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MovieDetail>>(UiState.Loading)
    val state: StateFlow<UiState<MovieDetail>> = _state.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    val isSaved: StateFlow<Boolean> = saved.isSaved(id, MediaType.MOVIE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        _notice.value = null
        viewModelScope.launch {
            _state.value = try {
                val rated = repository.movie(id)
                _notice.value = rated.notice
                UiState.Success(rated.detail)
            } catch (e: Exception) {
                UiState.Error(e.userMessage())
            }
        }
    }

    fun toggleSaved(summary: TitleSummary) {
        viewModelScope.launch { saved.toggle(summary) }
    }
}

class TvDetailViewModel(
    private val id: Int,
    private val repository: RatedTitleRepository,
    private val saved: SavedTitlesStore,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TvDetail>>(UiState.Loading)
    val state: StateFlow<UiState<TvDetail>> = _state.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<Episode?>(null)
    val selectedEpisode: StateFlow<Episode?> = _selectedEpisode.asStateFlow()

    private val _highlightSeason = MutableStateFlow<Int?>(null)
    val highlightSeason: StateFlow<Int?> = _highlightSeason.asStateFlow()

    val isSaved: StateFlow<Boolean> = saved.isSaved(id, MediaType.TV)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        _notice.value = null
        viewModelScope.launch {
            _state.value = try {
                val rated = repository.tv(id)
                _notice.value = rated.notice
                UiState.Success(rated.detail)
            } catch (e: Exception) {
                UiState.Error(e.userMessage())
            }
        }
    }

    fun selectEpisode(episode: Episode?) {
        _selectedEpisode.value = episode
    }

    fun toggleSeasonHighlight(seasonNumber: Int) {
        _highlightSeason.value = if (_highlightSeason.value == seasonNumber) null else seasonNumber
    }

    fun toggleSaved(summary: TitleSummary) {
        viewModelScope.launch { saved.toggle(summary) }
    }
}
