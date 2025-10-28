package com.roastkoff.displayposter.repository

import com.google.firebase.Timestamp

data class DisplayConfig(
    val version: Long = 0,
    val updatedAt: Timestamp? = null,
    val playlistId: String = "",
    val overrides: Map<String, Any>? = null
)