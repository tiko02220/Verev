package com.vector.verevcodex.domain.model.auth

data class SecuritySetup(
    val accountId: String,
    val pin: String,
    val biometricEnabled: Boolean,
)
