package com.roastkoff.displayposter.ui.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.displayposter.common.DisplayPreferences
import com.roastkoff.displayposter.repository.DisplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DisplayRepository,
    private val prefs: DisplayPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadDisplay()
//        viewModelScope.launch {
//            prefs.displayId.collect { id ->
//                if (id != null) {
//                    loadDisplay(id)
//                }
//            }
//        }
    }

    fun loadDisplay(displayId: String = "k5JlaY4UFpM8zxXA9QZy") {
        viewModelScope.launch {
            repository.listenDisplayConfig(displayId).collect { config ->
                Log.d("HomeViewModel", config.toString())
                if (config == null) return@collect
                val playlist = repository.loadPlaylist(config.playlistId)
                Log.d("HomeViewModel", playlist.toString())
                _uiState.value = _uiState.value.copy(
                    version = config.version.toLong(),
                    defaultIntervalMs = playlist?.defaultIntervalMs ?: 8000L,
                    items = playlist?.items ?: emptyList(),
                    lastSync = java.time.LocalTime.now().withNano(0).toString()
                )
            }
        }
    }

    fun toggleInfo(open: Boolean) {
        _uiState.value = _uiState.value.copy(infoOpen = open)
    }
}
