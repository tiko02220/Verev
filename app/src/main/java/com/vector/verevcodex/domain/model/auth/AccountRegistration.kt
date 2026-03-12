package com.vector.verevcodex.domain.model.auth

data class AccountRegistration(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)
