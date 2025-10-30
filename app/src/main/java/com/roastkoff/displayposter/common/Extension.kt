package com.roastkoff.displayposter.common

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Any?.toDateSafe(): Date? {
    return when (this) {
        is Timestamp -> this.toDate()
        is Long -> Date(this)
        is Number -> Date(this.toLong())
        is String -> {
            try {
                this.toLongOrNull()?.let { Date(it) }
                    ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        .apply { timeZone = TimeZone.getTimeZone("UTC") }
                        .parse(this)
            } catch (e: Exception) {
                Log.w("DateParse", "Failed to parse date: $this", e)
                null
            }
        }

        else -> null
    }
}

fun Any?.toLongSafe(default: Long = 0L): Long {
    return when (this) {
        is Long -> this
        is Number -> this.toLong()
        is String -> this.toLongOrNull() ?: default
        else -> default
    }
}