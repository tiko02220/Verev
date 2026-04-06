package com.vector.verevcodex.presentation.app

import com.vector.verevcodex.BuildConfig
import com.vector.verevcodex.data.remote.core.BackendEndpoint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.vector.verevcodex.platform.connectivity.rememberConnectivityStatus
import com.vector.verevcodex.presentation.auth.security.AppSecurityUiState
import com.vector.verevcodex.presentation.auth.security.SecurityUnlockScreen
import com.vector.verevcodex.presentation.navigation.AuthNavHost
import com.vector.verevcodex.presentation.navigation.MerchantAppNavHost
import com.vector.verevcodex.presentation.navigation.Screen
import com.vector.verevcodex.presentation.scan.ScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun AppEntryScreen(
    securityState: AppSecurityUiState,
    scanViewModel: ScanViewModel,
    onPinChanged: (String) -> Unit,
    onUseBiometric: () -> Unit,
    onBiometricResult: (Boolean) -> Unit,
    onRecoverAccess: () -> Unit,
    onExitPinRecovery: () -> Unit,
    onAuthFlowCompleted: () -> Unit,
    onLogout: () -> Unit,
) {
    val connectivityStatus = rememberConnectivityStatus()
    val destination = securityState.toRootDestination()
    var backendRetryToken by rememberSaveable { mutableStateOf(0) }
    var backendAvailable by rememberSaveable { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(connectivityStatus.isOnline, backendRetryToken) {
        if (!connectivityStatus.isOnline) {
            backendAvailable = false
        } else {
            val reachable = checkBackendReachability()
            backendAvailable = reachable
        }
    }

    if (!connectivityStatus.isOnline || backendAvailable != true) {
        AppBootstrapScreen(
            isOnline = connectivityStatus.isOnline && backendAvailable == null,
            onTryAgain = {
                connectivityStatus.refresh()
                if (connectivityStatus.isOnline) {
                    backendAvailable = null
                }
                backendRetryToken += 1
            },
        )
        return
    }

    when (destination) {
        AppRootDestination.Loading -> AppBootstrapScreen(
            isOnline = backendAvailable == true,
            onTryAgain = {
                connectivityStatus.refresh()
                backendRetryToken += 1
            },
        )

        is AppRootDestination.Auth -> key(destination.flowKey, destination.startDestination) {
            val authNavController = rememberNavController()
            AuthNavHost(
                navController = authNavController,
                startDestination = destination.startDestination,
                onForgotPinExit = onExitPinRecovery,
                onAuthenticated = onAuthFlowCompleted,
            )
        }

        AppRootDestination.Unlock -> SecurityUnlockScreen(
            state = securityState,
            onPinChanged = onPinChanged,
            onUseBiometric = onUseBiometric,
            onBiometricResult = onBiometricResult,
            onRecoverAccess = onRecoverAccess,
            onLogout = onLogout,
        )

        AppRootDestination.Merchant -> key(securityState.authEntryNonce, securityState.session?.user?.id) {
            MerchantAppNavHost(
                scanViewModel = scanViewModel,
                startDestination = Screen.Dashboard.route,
            )
        }
    }
}

private suspend fun checkBackendReachability(): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        val backendEndpoint = BackendEndpoint.from(BuildConfig.VEREV_BACKEND_BASE_URL)
        val connection = (URL("${backendEndpoint.httpBaseUrl}actuator/health").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 4_000
            readTimeout = 4_000
            instanceFollowRedirects = true
        }
        try {
            val code = connection.responseCode
            code in 200..299
        } finally {
            connection.disconnect()
        }
    }.getOrDefault(false)
}

@Composable
private fun AppBootstrapScreen(
    isOnline: Boolean,
    onTryAgain: () -> Unit,
) {
    var retrying by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isOnline) {
        if (isOnline) {
            retrying = false
        }
    }

    VerevSplashScreen(
        modifier = Modifier.fillMaxSize(),
        isOffline = !isOnline,
        showLoader = isOnline || retrying,
        onTryAgain = {
            retrying = true
            onTryAgain()
            scope.launch {
                delay(1200)
                retrying = false
            }
        },
    )
}
