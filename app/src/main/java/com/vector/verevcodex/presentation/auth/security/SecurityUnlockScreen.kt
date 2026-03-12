package com.vector.verevcodex.presentation.auth.security

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.auth.common.AuthCenteredSection
import com.vector.verevcodex.presentation.auth.common.AuthGradientScreenScaffold
import com.vector.verevcodex.presentation.auth.common.showBiometricPrompt
import kotlinx.coroutines.delay

@Composable
fun SecurityUnlockScreen(
    state: AppSecurityUiState,
    onPinChanged: (String) -> Unit,
    onUseBiometric: () -> Unit,
    onBiometricResult: (Boolean) -> Unit,
    onRecoverAccess: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var biometricRequestedForSession by remember { mutableStateOf(false) }
    var biometricPromptLaunched by remember { mutableStateOf(false) }
    var isScreenResumed by remember {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    var hasWindowFocus by remember { mutableStateOf(view.hasWindowFocus()) }
    val canPromptBiometric = activity != null && isScreenResumed && hasWindowFocus

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isScreenResumed = when (event) {
                Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_PAUSE -> false
                else -> lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    androidx.compose.runtime.DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnWindowFocusChangeListener { focused ->
            hasWindowFocus = focused
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            if (view.viewTreeObserver.isAlive) {
                view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
            }
        }
    }

    LaunchedEffect(state.requiresUnlock) {
        if (!state.requiresUnlock) {
            biometricRequestedForSession = false
            biometricPromptLaunched = false
        }
    }

    LaunchedEffect(state.promptBiometric) {
        if (!state.promptBiometric) {
            biometricPromptLaunched = false
        }
    }

    LaunchedEffect(state.requiresUnlock, state.securityConfig?.biometricEnabled, canPromptBiometric) {
        if (canPromptBiometric && state.requiresUnlock && state.securityConfig?.biometricEnabled == true && !biometricRequestedForSession) {
            biometricRequestedForSession = true
            onUseBiometric()
        }
    }

    LaunchedEffect(state.promptBiometric, canPromptBiometric, activity) {
        if (state.promptBiometric && activity != null && canPromptBiometric && !biometricPromptLaunched) {
            biometricPromptLaunched = true
            delay(250)
            showBiometricPrompt(
                activity = activity,
                titleRes = R.string.auth_biometric_title,
                subtitleRes = R.string.auth_biometric_subtitle,
                negativeRes = R.string.auth_biometric_negative,
                onResult = onBiometricResult,
            )
        }
    }

    AuthGradientScreenScaffold {
        Spacer(Modifier.height(16.dp))
        AuthCenteredSection(maxWidth = 520.dp) {
            SecurityBrandHeader()
        }
        Spacer(Modifier.height(24.dp))
        AuthCenteredSection(maxWidth = 520.dp) {
            SecurityUnlockCard(
                state = state,
                pinValue = state.pinDigits.joinToString(separator = ""),
                onPinChanged = onPinChanged,
                onUseBiometric = {
                    biometricRequestedForSession = true
                    onUseBiometric()
                },
                onRecoverAccess = onRecoverAccess,
                onLogoutRequested = { showLogoutDialog = true },
            )
        }
        Spacer(Modifier.height(14.dp))
        androidx.compose.material3.Text(
            text = "\uD83D\uDD12 ${stringResource(R.string.auth_unlock_secure_note)}",
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }

    if (showLogoutDialog) {
        SecurityLogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onCancel = { showLogoutDialog = false },
        )
    }
}
