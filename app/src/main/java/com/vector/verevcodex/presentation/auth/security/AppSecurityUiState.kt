package com.vector.verevcodex.presentation.auth.security

import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.SecurityConfig

data class AppSecurityUiState(
    val isInitialized: Boolean = false,
    val session: AuthSession? = null,
    val securityConfig: SecurityConfig? = null,
    val authEntryDestination: AuthEntryDestination? = null,
    val authEntryNonce: Int = 0,
    val requiresUnlock: Boolean = false,
    val pinDigits: List<String> = List(4) { "" },
    val pinError: String? = null,
    val pinErrorCount: Int = 0,
    val promptBiometric: Boolean = false,
)

enum class AuthEntryDestination {
    LOGIN,
    FORGOT_PIN,
}
