package com.roastkoff.displayposter.ui.screen

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roastkoff.displayposter.repository.DisplayRepository
import com.roastkoff.displayposter.repository.PlaylistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DisplayRepository,
    private val app: Application
) : ViewModel() {
    data class Ui(
        val playing: Boolean = true,
        val infoOpen: Boolean = false,
        val deviceName: String = "",
        val tenantId: String? = null,
        val branchId: String? = null,
        val lastSync: String = "--",
        val version: Long = 0,
        val defaultIntervalMs: Long = 8000,
        val items: List<PlaylistItem> = emptyList()
    )

    var ui by mutableStateOf(Ui())

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
//            repository.listenDisplayConfig(displayId).collect { cfg ->
//                if (cfg == null) return@collect
//                val playlist = repository.loadPlaylist(cfg.playlistId)
//                val interval = (cfg.overrides?.get("defaultIntervalMs") as? Number)?.toLong()
//                    ?: playlist?.defaultIntervalMs ?: 8000L
//                ui = ui.copy(
//                    version = cfg.version,
//                    defaultIntervalMs = interval,
//                    items = playlist?.items ?: emptyList(),
//                    lastSync = java.time.LocalTime.now().withNano(0).toString()
//                )
//            }
        }
    }

    fun toggleInfo(open: Boolean) {
        ui = ui.copy(infoOpen = open)
    }
}
