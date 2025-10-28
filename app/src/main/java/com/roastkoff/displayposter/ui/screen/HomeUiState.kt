package com.roastkoff.displayposter.ui.screen

import com.roastkoff.displayposter.repository.PlaylistItem

data class HomeUiState(
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