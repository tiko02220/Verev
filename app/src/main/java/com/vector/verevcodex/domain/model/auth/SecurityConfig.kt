package com.vector.verevcodex.domain.model.auth

data class SecurityConfig(
    val accountId: String,
    val pin: String,
    val biometricEnabled: Boolean,
    val hasQuickPin: Boolean = pin.isNotBlank(),
)
