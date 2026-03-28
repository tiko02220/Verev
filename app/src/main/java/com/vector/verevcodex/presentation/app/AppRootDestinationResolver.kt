package com.vector.verevcodex.presentation.app

import com.vector.verevcodex.presentation.auth.security.AppSecurityUiState
import com.vector.verevcodex.presentation.auth.security.AuthEntryDestination
import com.vector.verevcodex.presentation.navigation.Screen

internal fun AppSecurityUiState.toRootDestination(): AppRootDestination {
    if (!isInitialized) return AppRootDestination.Loading

    if (session != null) {
        if (signupOnboardingPending) {
            return AppRootDestination.Auth(
                startDestination = Screen.Signup.route,
                flowKey = authEntryNonce,
            )
        }

        if (session.user.requiresPasswordSetup) {
            return AppRootDestination.Auth(
                startDestination = Screen.ForcePasswordSetup.route,
                flowKey = authEntryNonce,
            )
        }

        if (securityConfig == null) {
            return AppRootDestination.Auth(
                startDestination = Screen.FirstSecuritySetup.route,
                flowKey = authEntryNonce,
            )
        }

        if (requiresUnlock && securityConfig != null) {
            return AppRootDestination.Unlock
        }

        if (authEntryDestination == AuthEntryDestination.FORGOT_PIN) {
            return AppRootDestination.Auth(
                startDestination = authEntryDestination.toRoute(),
                flowKey = authEntryNonce,
            )
        }

        return AppRootDestination.Merchant
    }

    authEntryDestination?.let { destination ->
        return AppRootDestination.Auth(
            startDestination = destination.toRoute(),
            flowKey = authEntryNonce,
        )
    }

    return AppRootDestination.Auth(
        startDestination = Screen.Login.route,
        flowKey = authEntryNonce,
    )
}

private fun AuthEntryDestination.toRoute(): String = when (this) {
    AuthEntryDestination.LOGIN -> Screen.Login.route
    AuthEntryDestination.SIGNUP -> Screen.Signup.route
    AuthEntryDestination.FORGOT_PIN -> Screen.ForgotPin.route
    AuthEntryDestination.FORCE_PASSWORD_SETUP -> Screen.ForcePasswordSetup.route
}
