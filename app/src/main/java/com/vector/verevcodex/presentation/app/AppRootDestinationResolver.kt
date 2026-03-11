package com.vector.verevcodex.presentation.app

import com.vector.verevcodex.presentation.auth.security.AppSecurityUiState
import com.vector.verevcodex.presentation.auth.security.AuthEntryDestination
import com.vector.verevcodex.presentation.navigation.Screen

internal fun AppSecurityUiState.toRootDestination(): AppRootDestination {
    if (!isInitialized) return AppRootDestination.Loading

    authEntryDestination?.let { destination ->
        return AppRootDestination.Auth(
            startDestination = destination.toRoute(),
            flowKey = authEntryNonce,
        )
    }

    if (session == null) {
        return AppRootDestination.Auth(
            startDestination = Screen.Login.route,
            flowKey = authEntryNonce,
        )
    }

    if (requiresUnlock && securityConfig != null) {
        return AppRootDestination.Unlock
    }

    return AppRootDestination.Merchant
}

private fun AuthEntryDestination.toRoute(): String = when (this) {
    AuthEntryDestination.LOGIN -> Screen.Login.route
    AuthEntryDestination.FORGOT_PIN -> Screen.ForgotPin.route
}
