package com.vector.verevcodex.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.vector.verevcodex.R
import com.vector.verevcodex.platform.connectivity.rememberConnectivityStatus
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
    val connectivityStatus = rememberConnectivityStatus()
    val destination = securityState.toRootDestination()

    if (!connectivityStatus.isOnline && destination.shouldShowOfflineBootstrap()) {
        AppBootstrapScreen(
            isOnline = false,
            onTryAgain = connectivityStatus.refresh,
        )
        return
    }

    when (destination) {
        AppRootDestination.Loading -> AppBootstrapScreen(
            isOnline = true,
            onTryAgain = connectivityStatus.refresh,
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

        AppRootDestination.Merchant -> MerchantAppNavHost(
            navController = merchantNavController,
            scanViewModel = scanViewModel,
            startDestination = Screen.Dashboard.route,
        )
    }
}

@Composable
private fun AppBootstrapScreen(
    isOnline: Boolean,
    onTryAgain: () -> Unit,
) {
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
    ) {
        if (isOnline) {
            VerevSplashScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                        .padding(24.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.splash_offline_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(R.string.splash_offline_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                        Button(
                            onClick = onTryAgain,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerevColors.Gold,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(stringResource(R.string.merchant_try_again))
                        }
                    }
                }
            }
        }
    }
}

private fun AppRootDestination.shouldShowOfflineBootstrap(): Boolean = when (this) {
    AppRootDestination.Loading -> true
    is AppRootDestination.Auth -> startDestination == Screen.Login.route
    AppRootDestination.Unlock -> false
    AppRootDestination.Merchant -> false
}
