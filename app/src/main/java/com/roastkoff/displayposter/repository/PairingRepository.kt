package com.roastkoff.displayposter.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

data class PairingClaimInfo(
    val tenantId: String,
    val groupId: String?,
    val displayId: String
)

sealed class PairingResult {
    object Waiting : PairingResult()
    data class Claimed(val info: PairingClaimInfo) : PairingResult()
    data class Error(val message: String) : PairingResult()
}

interface PairingRepository {
    suspend fun createPairingSession(): String

    fun listenPairing(code: String): Flow<PairingResult>
}

class PairingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PairingRepository {

    override suspend fun createPairingSession(): String {
        val code = generateCode()
        val now = Date()
        val expires = Date(now.time + 5 * 60_000) // 5 นาที

        val data = hashMapOf(
            "code" to code,
            "status" to "pending",
            "tenantId" to null,
            "groupId" to null,
            "displayId" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "expiresAt" to Timestamp(expires)
        )

        firestore.collection("pairingSessions")
            .document(code)
            .set(data)
            .await()

        return code
    }

    override fun listenPairing(code: String): Flow<PairingResult> = callbackFlow {
        val ref = firestore.collection("pairingSessions").document(code)

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(PairingResult.Error(error.message ?: "เกิดข้อผิดพลาดระหว่างรอการเชื่อมต่อ"))
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                return@addSnapshotListener
            }

            val status = snapshot.getString("status")
            if (status == "claimed") {
                val tenantId = snapshot.getString("tenantId") ?: ""
                val groupId = snapshot.getString("groupId")
                val displayId = snapshot.getString("displayId") ?: ""

                trySend(
                    PairingResult.Claimed(
                        PairingClaimInfo(
                            tenantId = tenantId,
                            groupId = groupId,
                            displayId = displayId
                        )
                    )
                )
            } else {
                trySend(PairingResult.Waiting)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    private fun generateCode(): String {
        val number = (100_000..999_999).random()
        return number.toString()
    }
}