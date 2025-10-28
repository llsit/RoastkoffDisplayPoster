package com.roastkoff.displayposter.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.displayposter.repository.DisplayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DisplayRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var listenJob: Job? = null

    init {
        start()
    }

    fun start() {
//        val prefs = Prefs(app)
//        val displayId = prefs.displayId ?: return
//        ui = ui.copy(
//            deviceName = displayId.take(6),
//            tenantId = prefs.tenantId,
//            branchId = prefs.branchId
//        )

        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            repository.listenDisplayConfig("displayId").collect { cfg ->
                if (cfg == null) return@collect
                val playlist = repository.loadPlaylist(cfg.playlistId)
                val interval = (cfg.overrides?.get("defaultIntervalMs") as? Number)?.toLong()
                    ?: playlist?.defaultIntervalMs ?: 8000L
                _uiState.value = _uiState.value.copy(
                    version = cfg.version,
                    defaultIntervalMs = interval,
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
