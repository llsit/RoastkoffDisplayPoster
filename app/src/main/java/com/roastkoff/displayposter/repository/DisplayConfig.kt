package com.roastkoff.displayposter.repository

import com.google.firebase.Timestamp

data class DisplayConfig(
    val version: String = "",
    val updatedAt: Timestamp? = null,
    val playlistId: String = "",
    val overrides: ArrayList<String>? = null
)