package com.vector.verevcodex.domain.repository.auth

import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.AuthBootstrapState
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.model.auth.SignupOnboardingProgress
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthBootstrapState(): Flow<AuthBootstrapState>
    fun observeSession(): Flow<AuthSession?>
    fun observeCurrentSecurityConfig(): Flow<SecurityConfig?>
    fun observeEmailNotificationSettings(): Flow<EmailNotificationSettings?>
    fun observeSignupOnboardingPending(): Flow<Boolean>
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun registerBusiness(business: BusinessRegistration, account: AccountRegistration): Result<RegistrationResult>
    suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit>
    suspend fun updateCurrentProfile(update: PersonalInformationUpdate): Result<Unit>
    suspend fun changeCurrentPassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun activateInvitedPassword(newPassword: String): Result<Unit>
    suspend fun updateCurrentQuickPin(currentPin: String, newPin: String): Result<Unit>
    suspend fun updateCurrentBiometricEnabled(enabled: Boolean): Result<Unit>
    suspend fun updateEmailNotificationSettings(settings: EmailNotificationSettings): Result<Unit>
    suspend fun verifyQuickPin(pin: String): Boolean
    suspend fun sendPasswordResetCode(email: String): Result<Unit>
    suspend fun sendQuickPinResetCode(email: String): Result<Unit>
    suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit>
    suspend fun verifyQuickPinResetCode(email: String, code: String): Result<Unit>
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit>
    suspend fun resetQuickPin(email: String, newPin: String): Result<Unit>
    suspend fun activateSession(accountId: String): Result<Unit>
    suspend fun setSignupOnboardingPending(pending: Boolean)
    suspend fun updateSignupOnboardingProgress(progress: SignupOnboardingProgress?)
    suspend fun logout()
}
