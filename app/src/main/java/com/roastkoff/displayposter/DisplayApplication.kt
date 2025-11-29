package com.roastkoff.displayposter

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.roastkoff.displayposter.common.DisplayPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class DisplayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        signInAnonymously()
    }

    private fun signInAnonymously() {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val pref = DisplayPreferences(this)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d("DisplayPosterApp", "Already signed in: ${auth.currentUser?.uid}")
            return
        }
        Log.d("DisplayPosterApp", "Signing in anonymously...")
        val latch = CountDownLatch(1)
        auth.signInAnonymously().addOnSuccessListener { result ->
            Log.d(
                "DisplayPosterApp",
                "✅ Signed in anonymously: ${result.user?.uid}"
            )
            applicationScope.launch {
                val userId = result.user?.uid
                if (userId != null) {
                    pref.saveUserId(userId)
                }
            }
            latch.countDown()
        }.addOnFailureListener { exception ->
            Log.e(
                "DisplayPosterApp",
                "❌ Anonymous sign-in failed",
                exception
            )
            latch.countDown()
        }
        try {
            latch.await(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Log.e("DisplayPosterApp", "Sign-in interrupted", e)
        }
    }
}