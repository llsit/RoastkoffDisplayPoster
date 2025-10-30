package com.roastkoff.displayposter.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.roastkoff.displayposter.common.Resource
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface DisplayRepository {
    fun listenDisplayConfig(displayId: String): Flow<Resource<DisplayConfig>>
    suspend fun loadPlaylist(playlistId: String): Flow<Resource<Playlist?>>
}

class DisplayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DisplayRepository {
    override fun listenDisplayConfig(displayId: String): Flow<Resource<DisplayConfig>> =
        callbackFlow {
            val data = firestore.collection("displayConfigs")
                .document(displayId)
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        Log.e("DisplayRepository", "listenDisplayConfig error", e)
                        trySend(Resource.Error(e))
                        return@addSnapshotListener
                    }
                    try {
                        val config = snap?.toObject(DisplayConfigDto::class.java)
                            ?.toDomain()

                        if (config != null) {
                            trySend(Resource.Success(config))
                        } else {
                            trySend(Resource.Error(Exception("Document not found")))
                        }
                    } catch (e: Exception) {
                        Log.e("DisplayRepository", "Parse error", e)
                        trySend(Resource.Error(e))
                    }
                }

            awaitClose { data.remove() }
        }

    override suspend fun loadPlaylist(playlistId: String): Flow<Resource<Playlist?>> =
        callbackFlow {
            val document = firestore.collection("playlists")
                .document(playlistId)
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        Log.e("DisplayRepository", "loadPlaylist error", e)
                        trySend(Resource.Error(e))
                        return@addSnapshotListener
                    }

                    try {
                        val playlistItem =
                            (snap?.data?.get("items") as? List<*>)?.mapNotNull { any ->
                                val item = any as? Map<*, *> ?: return@mapNotNull null
                                PlaylistItemDto(
                                    id = item["id"] as? String ?: "",
                                    type = item["type"] as? String ?: "",
                                    src = item["src"] as? String ?: "",
                                    durationMs = (item["durationMs"] as? Number)?.toLong(),
                                    fit = item["fit"] as? String,
                                    mute = item["mute"] as? Boolean,
                                    volume = (item["volume"] as? Number)?.toDouble()
                                ).toDomain()
                            } ?: emptyList()
                        val playlist = snap?.toObject(PlaylistDto::class.java)
                            ?.toDomain()
                        val result = playlist?.copy(items = playlistItem)
                        if (result != null) {
                            trySend(Resource.Success(playlist))
                        } else {
                            trySend(Resource.Error(Exception("Document not found")))
                        }
                    } catch (e: Exception) {
                        Log.e("DisplayRepository", "Parse error", e)
                        trySend(Resource.Error(e))
                    }
                }

            awaitClose { document.remove() }
        }

}