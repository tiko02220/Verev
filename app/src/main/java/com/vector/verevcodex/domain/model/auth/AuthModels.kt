package com.vector.verevcodex.domain.model.auth

import com.vector.verevcodex.domain.model.StaffRole

data class AuthUser(
    val id: String,
    val relatedEntityId: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val role: StaffRole,
    val active: Boolean,
)

data class AuthSession(
    val user: AuthUser,
    val isAuthenticated: Boolean,
)

data class BusinessRegistration(
    val businessName: String,
    val industry: String,
    val address: String,
    val city: String,
    val zipCode: String,
    val phoneNumber: String,
)

data class AccountRegistration(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)

data class RegistrationResult(
    val session: AuthSession,
    val businessId: String,
    val defaultStoreId: String,
)

data class PasswordResetRequest(
    val email: String,
)

data class SecuritySetup(
    val accountId: String,
    val pin: String,
    val biometricEnabled: Boolean,
)

data class SecurityConfig(
    val accountId: String,
    val pin: String,
    val biometricEnabled: Boolean,
)

data class StaffOnboardingMember(
    val fullName: String,
    val email: String,
    val password: String,
    val role: StaffRole,
    val permissionsSummary: String,
)
