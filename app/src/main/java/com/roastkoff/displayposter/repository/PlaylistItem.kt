package com.roastkoff.displayposter.repository

data class PlaylistItem(
    val id: String = "",
    val type: String = "image",
    val src: String = "",
    val durationMs: Long? = null,
    val mute: Boolean? = null,
    val volume: Double? = null,
    val fit: String? = "contain",
    val transition: String? = "fade"
)