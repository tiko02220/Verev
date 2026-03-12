package com.vector.verevcodex.domain.model.auth

data class AuthSession(
    val user: AuthUser,
    val isAuthenticated: Boolean,
)
