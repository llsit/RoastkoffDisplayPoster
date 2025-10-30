package com.roastkoff.displayposter.repository

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistItem(
    val id: String,
    val type: PlaylistItemType,
    val src: String,
    val durationMs: Long?,
    val mute: Boolean,
    val volume: Double,
    val fit: PlaylistItemFit,
    val transition: PlaylistItemTransition
)

@Serializable
data class PlaylistItemDto(
    val id: String = "",
    val type: String = "image",
    val src: String = "",
    val durationMs: Any? = null,
    val mute: Boolean? = null,
    val volume: Any? = null,
    val fit: String? = "contain",
    val transition: String? = "fade"
)

fun PlaylistItemDto.toDomain(): PlaylistItem {
    return PlaylistItem(
        id = id,
        type = when (type.lowercase()) {
            "image" -> PlaylistItemType.IMAGE
            "video" -> PlaylistItemType.VIDEO
            else -> PlaylistItemType.UNKNOWN
        },
        src = src,
        durationMs = when (durationMs) {
            is Long -> durationMs
            is Number -> durationMs.toLong()
            is String -> durationMs.toLongOrNull()
            else -> null
        },
        mute = mute ?: false,
        volume = when (volume) {
            is Double -> volume
            is Number -> volume.toDouble()
            is String -> volume.toDoubleOrNull() ?: 1.0
            else -> 1.0
        },
        fit = when (fit?.lowercase()) {
            "contain" -> PlaylistItemFit.CONTAIN
            "cover" -> PlaylistItemFit.COVER
            "fill" -> PlaylistItemFit.FILL
            "scale-down", "scale_down" -> PlaylistItemFit.SCALE_DOWN
            "none" -> PlaylistItemFit.NONE
            else -> PlaylistItemFit.CONTAIN
        },
        transition = when (transition?.lowercase()) {
            "fade" -> PlaylistItemTransition.FADE
            "slide" -> PlaylistItemTransition.SLIDE
            "none" -> PlaylistItemTransition.NONE
            else -> PlaylistItemTransition.FADE
        }
    )
}