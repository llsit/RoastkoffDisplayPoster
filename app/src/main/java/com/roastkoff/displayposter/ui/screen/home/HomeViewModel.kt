package com.roastkoff.displayposter.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.displayposter.common.DisplayPreferences
import com.roastkoff.displayposter.common.Resource
import com.roastkoff.displayposter.repository.DisplayConfig
import com.roastkoff.displayposter.repository.DisplayRepository
import com.roastkoff.displayposter.repository.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DisplayRepository,
    private val prefs: DisplayPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            prefs.displayId.collect { id ->
                if (id != null) {
                    loadDisplay(id)
                }
            }
        }
    }

    fun loadDisplay(displayId: String) {
        viewModelScope.launch {
            repository.listenDisplayConfig(displayId).collect { configResult ->
                when (configResult) {
                    is Resource.Error -> {}
                    Resource.Loading -> {}
                    is Resource.Success<DisplayConfig> -> {
                        val config = configResult.data
                        if (config.activePlaylistId.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                version = config.version,
                                defaultIntervalMs = 8000L,
                                items = emptyList(),
                                lastSync = LocalTime.now().withNano(0).toString()
                            )
                        } else {
                            repository.loadPlaylist(config.activePlaylistId).collect { playlist ->
                                when (playlist) {
                                    is Resource.Error -> {}
                                    Resource.Loading -> {}
                                    is Resource.Success<Playlist?> -> {
                                        _uiState.value = _uiState.value.copy(
                                            version = config.version,
                                            defaultIntervalMs = playlist.data?.defaultIntervalMs
                                                ?: 8000L,
                                            items = playlist.data?.items ?: emptyList(),
                                            lastSync = LocalTime.now().withNano(0)
                                                .toString()
                                        )
                                    }
                                }
                            }

                        }

                    }
                }
            }
        }
    }

    fun toggleInfo(open: Boolean) {
        _uiState.value = _uiState.value.copy(infoOpen = open)
    }
}
