package com.vector.verevcodex

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.forgot.ForgotPasswordScreen
import com.vector.verevcodex.presentation.auth.forgot.RecoveryMode
import com.vector.verevcodex.presentation.auth.security.AuthEntryDestination
import com.vector.verevcodex.presentation.auth.security.AppSecurityViewModel
import com.vector.verevcodex.presentation.auth.security.SecurityExitDestination
import com.vector.verevcodex.presentation.auth.security.SecurityUnlockScreen
import com.vector.verevcodex.presentation.navigation.AuthNavHost
import com.vector.verevcodex.presentation.navigation.MerchantAppNavHost
import com.vector.verevcodex.presentation.navigation.Screen
import com.vector.verevcodex.presentation.scan.ScanViewModel
import com.vector.verevcodex.presentation.theme.VerevMerchantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val scanViewModel: ScanViewModel by viewModels()
    private val appSecurityViewModel: AppSecurityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleNfcIntent(intent)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    VerevMerchantTheme {
                        val pendingNfcId by scanViewModel.pendingNfcId.collectAsStateWithLifecycle()
                        val securityState by appSecurityViewModel.uiState.collectAsStateWithLifecycle()
                        val appNavController = rememberNavController()
                        val inAuthMode = securityState.isInitialized &&
                            (securityState.session == null || securityState.exitDestination != null)
                        LaunchedEffect(pendingNfcId) { }
                        if (securityState.isInitialized) {
                            if (inAuthMode) {
                                if (securityState.authEntryDestination == AuthEntryDestination.FORGOT_PIN) {
                                    LaunchedEffect(securityState.exitDestination) {
                                        if (securityState.exitDestination == SecurityExitDestination.FORGOT_PIN) {
                                            appSecurityViewModel.consumeExitDestination()
                                        }
                                    }
                                    ForgotPasswordScreen(
                                        mode = RecoveryMode.PIN,
                                        onBackToLogin = appSecurityViewModel::showLoginAuthScreen,
                                    )
                                } else {
                                    key(securityState.authEntryNonce) {
                                        val authNavController = rememberNavController()
                                        val currentAuthRoute = authNavController.currentBackStackEntryAsState().value?.destination?.route
                                        AuthNavHost(
                                            navController = authNavController,
                                            startDestination = Screen.Login.route,
                                            onAuthenticated = { },
                                        )
                                        LaunchedEffect(securityState.exitDestination, currentAuthRoute) {
                                            if (securityState.exitDestination == SecurityExitDestination.LOGIN &&
                                                currentAuthRoute == Screen.Login.route
                                            ) {
                                                appSecurityViewModel.consumeExitDestination()
                                            }
                                        }
                                    }
                                }
                            } else {
                                MerchantAppNavHost(
                                    navController = appNavController,
                                    scanViewModel = scanViewModel,
                                    startDestination = Screen.Dashboard.route,
                                )
                            }
                        }
                        if (securityState.exitDestination != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                colorResource(R.color.brand_forest_deep),
                                                colorResource(R.color.brand_green),
                                            )
                                        )
                                    )
                            )
                        }
                        if (securityState.requiresUnlock) {
                            SecurityUnlockScreen(
                                state = securityState,
                                onPinChanged = appSecurityViewModel::updatePinCode,
                                onUseBiometric = appSecurityViewModel::requestBiometric,
                                onBiometricResult = appSecurityViewModel::biometricHandled,
                                onRecoverAccess = appSecurityViewModel::recoverAccess,
                                onLogout = appSecurityViewModel::logoutToLogin,
                            )
                        }
                    }
                }
            }
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        appSecurityViewModel.onAppForegrounded(SystemClock.elapsedRealtime())
    }

    override fun onStop() {
        super.onStop()
        appSecurityViewModel.onAppBackgrounded(SystemClock.elapsedRealtime())
    }

    private fun handleNfcIntent(intent: Intent?) {
        val action = intent?.action ?: return
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED || action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                ?.joinToString(separator = "") { byte -> "%02X".format(byte) }
                ?: return
            scanViewModel.onExternalNfcScan(tagId)
        }
    }
}
