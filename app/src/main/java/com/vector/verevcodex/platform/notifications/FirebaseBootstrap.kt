package com.vector.verevcodex.platform.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.vector.verevcodex.BuildConfig

object FirebaseBootstrap {
    private const val TAG = "FirebaseBootstrap"

    fun initialize(context: Context) {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            return
        }
        val projectId = BuildConfig.FIREBASE_PROJECT_ID.trim()
        val applicationId = BuildConfig.FIREBASE_APPLICATION_ID.trim()
        val apiKey = BuildConfig.FIREBASE_API_KEY.trim()
        val gcmSenderId = BuildConfig.FIREBASE_GCM_SENDER_ID.trim()
        if (projectId.isBlank() || applicationId.isBlank() || apiKey.isBlank() || gcmSenderId.isBlank()) {
            Log.i(TAG, "Firebase config is not set; push messaging remains disabled")
            return
        }
        FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setProjectId(projectId)
                .setApplicationId(applicationId)
                .setApiKey(apiKey)
                .setGcmSenderId(gcmSenderId)
                .build(),
        )
    }
}
