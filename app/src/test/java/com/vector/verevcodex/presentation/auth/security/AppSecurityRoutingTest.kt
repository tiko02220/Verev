package com.vector.verevcodex.presentation.auth.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppSecurityRoutingTest {

    @Test
    fun `signup onboarding keeps auth flow active even with session`() {
        val destination = resolveAuthEntryDestination(
            currentDestination = null,
            hasSession = true,
            signupOnboardingPending = true,
        )

        assertEquals(AuthEntryDestination.SIGNUP, destination)
    }

    @Test
    fun `existing session enters merchant flow when signup onboarding is complete`() {
        val destination = resolveAuthEntryDestination(
            currentDestination = null,
            hasSession = true,
            signupOnboardingPending = false,
        )

        assertNull(destination)
    }

    @Test
    fun `signup route is cleared once onboarding completes for an active session`() {
        val destination = resolveAuthEntryDestination(
            currentDestination = AuthEntryDestination.SIGNUP,
            hasSession = true,
            signupOnboardingPending = false,
        )

        assertNull(destination)
    }

    @Test
    fun `forgot pin keeps priority over signup onboarding`() {
        val destination = resolveAuthEntryDestination(
            currentDestination = AuthEntryDestination.FORGOT_PIN,
            hasSession = true,
            signupOnboardingPending = true,
        )

        assertEquals(AuthEntryDestination.FORGOT_PIN, destination)
    }

    @Test
    fun `completed signup does not re-enter signup while pending flag is clearing`() {
        val destination = resolveAuthEntryDestination(
            currentDestination = AuthEntryDestination.SIGNUP,
            hasSession = true,
            signupOnboardingPending = true,
            suppressSignupReentry = true,
        )

        assertNull(destination)
    }
}
