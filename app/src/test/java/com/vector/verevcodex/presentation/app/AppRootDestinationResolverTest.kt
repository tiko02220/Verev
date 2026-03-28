package com.vector.verevcodex.presentation.app

import com.vector.verevcodex.presentation.auth.security.AppSecurityUiState
import com.vector.verevcodex.presentation.auth.security.AuthEntryDestination
import com.vector.verevcodex.presentation.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class AppRootDestinationResolverTest {

    private fun signedInState(
        signupOnboardingPending: Boolean,
        authEntryDestination: AuthEntryDestination? = null,
        requiresUnlock: Boolean = false,
        requiresPasswordSetup: Boolean = false,
        hasSecurityConfig: Boolean = false,
    ) = AppSecurityUiState(
        isInitialized = true,
        session = com.vector.verevcodex.domain.model.auth.AuthSession(
            user = com.vector.verevcodex.domain.model.auth.AuthUser(
                id = "user-1",
                relatedEntityId = "user-1",
                fullName = "Owner",
                email = "owner@example.com",
                phoneNumber = "+37499111222",
                profilePhotoUri = "",
                role = com.vector.verevcodex.domain.model.common.StaffRole.OWNER,
                status = if (requiresPasswordSetup) "INVITED" else "ACTIVE",
                active = true,
                requiresPasswordSetup = requiresPasswordSetup,
            ),
            isAuthenticated = true,
        ),
        signupOnboardingPending = signupOnboardingPending,
        authEntryDestination = authEntryDestination,
        authEntryNonce = 7,
        requiresUnlock = requiresUnlock,
        securityConfig = if (requiresUnlock || hasSecurityConfig) {
            com.vector.verevcodex.domain.model.auth.SecurityConfig(
                accountId = "user-1",
                pin = "1234",
                biometricEnabled = false,
                hasQuickPin = true,
            )
        } else {
            null
        },
    )

    @Test
    fun `signup destination stays in auth flow while onboarding is pending`() {
        val destination = signedInState(
            signupOnboardingPending = true,
            authEntryDestination = AuthEntryDestination.SIGNUP,
        ).toRootDestination()

        assertEquals(
            AppRootDestination.Auth(
                startDestination = Screen.Signup.route,
                flowKey = 7,
            ),
            destination,
        )
    }

    @Test
    fun `signup onboarding keeps root in auth flow even when auth entry destination is temporarily cleared`() {
        val destination = signedInState(
            signupOnboardingPending = true,
            authEntryDestination = null,
        ).copy(authEntryNonce = 3).toRootDestination()

        assertEquals(
            AppRootDestination.Auth(
                startDestination = Screen.Signup.route,
                flowKey = 3,
            ),
            destination,
        )
    }

    @Test
    fun `completed signup ignores stale signup auth destination and stays in merchant`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            authEntryDestination = AuthEntryDestination.SIGNUP,
            hasSecurityConfig = true,
        ).toRootDestination()

        assertEquals(AppRootDestination.Merchant, destination)
    }

    @Test
    fun `authenticated session ignores stale login auth destination and stays in merchant`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            authEntryDestination = AuthEntryDestination.LOGIN,
            hasSecurityConfig = true,
        ).toRootDestination()

        assertEquals(AppRootDestination.Merchant, destination)
    }

    @Test
    fun `forgot pin remains routable for authenticated session`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            authEntryDestination = AuthEntryDestination.FORGOT_PIN,
            hasSecurityConfig = true,
        ).toRootDestination()

        assertEquals(
            AppRootDestination.Auth(
                startDestination = Screen.ForgotPin.route,
                flowKey = 7,
            ),
            destination,
        )
    }

    @Test
    fun `invited session routes to forced password setup before merchant`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            requiresPasswordSetup = true,
        ).toRootDestination()

        assertEquals(
            AppRootDestination.Auth(
                startDestination = Screen.ForcePasswordSetup.route,
                flowKey = 7,
            ),
            destination,
        )
    }

    @Test
    fun `authenticated session without quick pin routes to first security setup`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            requiresPasswordSetup = false,
            hasSecurityConfig = false,
        ).toRootDestination()

        assertEquals(
            AppRootDestination.Auth(
                startDestination = Screen.FirstSecuritySetup.route,
                flowKey = 7,
            ),
            destination,
        )
    }

    @Test
    fun `authenticated session with quick pin can enter merchant`() {
        val destination = signedInState(
            signupOnboardingPending = false,
            requiresPasswordSetup = false,
            hasSecurityConfig = true,
        ).toRootDestination()

        assertEquals(AppRootDestination.Merchant, destination)
    }
}
