package com.mobees.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobees.app.data.SettingsStore
import com.mobees.app.data.ratings.RatingsProvider
import com.mobees.app.data.ratings.defaultRatingsProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsStore,
    val hasTmdbKey: Boolean,
    val hasOmdbKey: Boolean,
) : ViewModel() {

    val ratingsProvider: StateFlow<RatingsProvider> = settings.ratingsProvider
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), defaultRatingsProvider(hasOmdbKey))

    fun setRatingsProvider(provider: RatingsProvider) {
        viewModelScope.launch { settings.setRatingsProvider(provider) }
    }
}
