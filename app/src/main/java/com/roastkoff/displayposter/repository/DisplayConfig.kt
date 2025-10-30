package com.roastkoff.displayposter.repository

import com.roastkoff.displayposter.common.toDateSafe
import com.roastkoff.displayposter.common.toLongSafe
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class DisplayConfig(
    val version: Long,
    val updatedAt: Date?,
    val playlistId: String,
    val overrides: List<String>
)

@Serializable
data class DisplayConfigDto(
    val version: Any? = null,
    val updatedAt: Any? = null,
    val playlistId: String? = null,
    val overrides: List<String>? = null
)

fun DisplayConfigDto.toDomain(): DisplayConfig {
    return DisplayConfig(
        version = version.toLongSafe(0L),
        updatedAt = updatedAt?.toDateSafe(),
        playlistId = playlistId ?: "",
        overrides = overrides ?: emptyList()
    )
}
