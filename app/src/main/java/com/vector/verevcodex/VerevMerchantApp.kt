package com.vector.verevcodex

import android.app.Application
import com.vector.verevcodex.platform.notifications.FirebaseBootstrap
import com.vector.verevcodex.platform.notifications.PushTokenSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VerevMerchantApp : Application() {
    @Inject lateinit var pushTokenSyncManager: PushTokenSyncManager

    override fun onCreate() {
        super.onCreate()
        FirebaseBootstrap.initialize(this)
        pushTokenSyncManager.start()
    }
}
