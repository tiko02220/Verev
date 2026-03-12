package com.vector.verevcodex.domain.usecase.auth

import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.repository.auth.AuthRepository

class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeSession()
}

class ObserveCurrentSecurityConfigUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeCurrentSecurityConfig()
}

class ObserveEmailNotificationSettingsUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeEmailNotificationSettings()
}

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}

class RegisterBusinessUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(business: BusinessRegistration, account: AccountRegistration) = repository.registerBusiness(business, account)
}

class SaveSecuritySetupUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(setup: SecuritySetup) = repository.saveSecuritySetup(setup)
}

class UpdateCurrentProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(update: PersonalInformationUpdate) = repository.updateCurrentProfile(update)
}

class ChangeCurrentPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) =
        repository.changeCurrentPassword(currentPassword, newPassword)
}

class UpdateCurrentQuickPinUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPin: String, newPin: String) =
        repository.updateCurrentQuickPin(currentPin, newPin)
}

class UpdateCurrentBiometricEnabledUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateCurrentBiometricEnabled(enabled)
}

class UpdateEmailNotificationSettingsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(settings: EmailNotificationSettings) =
        repository.updateEmailNotificationSettings(settings)
}

class VerifyQuickPinUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(pin: String) = repository.verifyQuickPin(pin)
}

class SendPasswordResetCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetCode(email)
}

class VerifyPasswordResetCodeUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String) = repository.verifyPasswordResetCode(email, code)
}

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, newPassword: String) = repository.resetPassword(email, newPassword)
}

class ResetQuickPinUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, newPin: String) = repository.resetQuickPin(email, newPin)
}

class ActivateSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(accountId: String) = repository.activateSession(accountId)
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
