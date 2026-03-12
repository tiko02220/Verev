package com.vector.verevcodex.domain.model.auth

data class RegistrationResult(
    val session: AuthSession,
    val businessId: String,
    val defaultStoreId: String,
)
