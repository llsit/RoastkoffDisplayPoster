package com.roastkoff.displayposter.repository

import com.roastkoff.displayposter.common.toLongSafe
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDto(
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val defaultIntervalMs: Any? = null,
    val loop: Boolean = true,
    val shuffle: Boolean = false,
    val items: List<PlaylistItemDto> = emptyList()
)

@Serializable
data class Playlist(
    val tenantId: String = "",
    val branchId: String? = null,
    val name: String = "",
    val defaultIntervalMs: Long = 8000,
    val loop: Boolean = true,
    val shuffle: Boolean = false,
    val items: List<PlaylistItem> = emptyList()
)

fun PlaylistDto.toDomain(): Playlist {
    return Playlist(
        tenantId = tenantId,
        branchId = branchId,
        name = name,
        defaultIntervalMs = defaultIntervalMs.toLongSafe(8000),
        loop = loop,
        shuffle = shuffle,
        items = items.map { it.toDomain() }
    )
}

enum class PlaylistItemType {
    IMAGE, VIDEO, UNKNOWN
}

enum class PlaylistItemFit {
    CONTAIN, COVER, FILL, SCALE_DOWN, NONE
}

enum class PlaylistItemTransition {
    FADE, SLIDE, NONE
}
