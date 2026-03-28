package com.vector.verevcodex.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.compose.rememberNavController
import com.vector.verevcodex.presentation.auth.security.AppSecurityUiState
import com.vector.verevcodex.presentation.auth.security.SecurityUnlockScreen
import com.vector.verevcodex.presentation.navigation.AuthNavHost
import com.vector.verevcodex.presentation.navigation.MerchantAppNavHost
import com.vector.verevcodex.presentation.navigation.Screen
import com.vector.verevcodex.presentation.scan.ScanViewModel
import com.vector.verevcodex.presentation.theme.VerevColors

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
    val merchantNavController = rememberNavController()

    when (val destination = securityState.toRootDestination()) {
        AppRootDestination.Loading -> AppBootstrapScreen()

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

        AppRootDestination.Merchant -> MerchantAppNavHost(
            navController = merchantNavController,
            scanViewModel = scanViewModel,
            startDestination = Screen.Dashboard.route,
        )
    }
}

@Composable
private fun AppBootstrapScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        VerevColors.ForestDeep,
                        VerevColors.Forest,
                        VerevColors.Moss,
                    )
                )
            )
    )
}
