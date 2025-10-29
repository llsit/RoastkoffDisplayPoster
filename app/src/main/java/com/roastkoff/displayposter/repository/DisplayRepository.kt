package com.roastkoff.displayposter.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface DisplayRepository {
    fun listenDisplayConfig(displayId: String): Flow<DisplayConfig?>
    suspend fun loadPlaylist(playlistId: String): Playlist?
}

class DisplayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DisplayRepository {
    override fun listenDisplayConfig(displayId: String): Flow<DisplayConfig?> = callbackFlow {
        val data = firestore.collection("displayConfigs")
            .document(displayId)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e("DisplayRepository", e.message.toString())
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snap?.toObject<DisplayConfig>())
            }

        awaitClose { data.remove() }
    }

    override suspend fun loadPlaylist(playlistId: String): Playlist? {
        val document = firestore.collection("playlists")
            .document(playlistId)
            .get()
            .await()

        return document.toObject<Playlist>()
    }

}