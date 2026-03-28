package com.vector.verevcodex.platform.notifications

import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.vector.verevcodex.BuildConfig
import com.vector.verevcodex.data.remote.notifications.NotificationRemoteDataSource
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class PushTokenSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val notificationRemoteDataSource: NotificationRemoteDataSource,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            authRepository.observeAuthBootstrapState()
                .map { state -> if (state.session != null && !state.signupOnboardingPending) state.session.user.id else null }
                .distinctUntilChanged()
                .collectLatest { accountId ->
                    if (accountId != null) {
                        syncCurrentToken()
                    }
                }
        }
    }

    fun onNewToken(token: String) {
        scope.launch {
            registerTokenIfAuthenticated(token)
        }
    }

    private suspend fun syncCurrentToken() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            return
        }
        val token = withContext(Dispatchers.IO) { FirebaseMessaging.getInstance().token.awaitResult() }
        registerTokenIfAuthenticated(token)
    }

    private suspend fun registerTokenIfAuthenticated(token: String) {
        if (token.isBlank()) {
            return
        }
        val isReady = authRepository.observeAuthBootstrapState()
            .map { state -> state.session != null && !state.signupOnboardingPending }
            .firstOrNull() == true
        if (!isReady) {
            return
        }
        notificationRemoteDataSource.registerPushDevice(
            deviceToken = token,
            platform = "ANDROID",
            appVersion = BuildConfig.VERSION_NAME,
            deviceModel = listOf(Build.MANUFACTURER, Build.MODEL).filter { it.isNotBlank() }.joinToString(" "),
            locale = Locale.getDefault().toLanguageTag(),
        )
    }
}
