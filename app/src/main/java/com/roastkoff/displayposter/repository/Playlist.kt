package com.roastkoff.displayposter.repository

data class Playlist(
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val defaultIntervalMs: Long = 8000,
    val loop: Boolean = true,
    val shuffle: Boolean = false,
    val items: List<PlaylistItem> = emptyList()
)