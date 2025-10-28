package com.roastkoff.displayposter.repository

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class DisplayConfig(
    val version: Long = 0,
    val updatedAt: com.google.firebase.Timestamp? = null,
    val playlistId: String = "",
    val overrides: Map<String, Any>? = null
)

data class Playlist(
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val defaultIntervalMs: Long = 8000,
    val loop: Boolean = true,
    val shuffle: Boolean = false,
    val items: List<PlaylistItem> = emptyList()
)

interface DisplayRepository {
    fun listenDisplayConfig(displayId: String): Flow<DisplayConfig?>
    suspend fun loadPlaylist(playlistId: String): Playlist?
}

class DisplayRepositoryImpl @Inject constructor() : DisplayRepository {
    override fun listenDisplayConfig(displayId: String): Flow<DisplayConfig?> {
        return flowOf(null)
    }

    override suspend fun loadPlaylist(playlistId: String): Playlist? {
       return null
    }

}