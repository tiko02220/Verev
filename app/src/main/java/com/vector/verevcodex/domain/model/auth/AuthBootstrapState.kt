package com.vector.verevcodex.domain.model.auth

data class AuthBootstrapState(
    val session: AuthSession?,
    val securityConfig: SecurityConfig?,
    val signupOnboardingPending: Boolean,
    val signupProgress: SignupOnboardingProgress? = null,
)

data class SignupOnboardingProgress(
    val accountId: String,
    val storeId: String?,
    val stage: SignupOnboardingStage,
    val pinSetupSkipped: Boolean,
)

enum class SignupOnboardingStage {
    PIN,
    BIOMETRIC,
    STAFF_PROMPT,
    STAFF_FORM,
    COMPLETE,
}
