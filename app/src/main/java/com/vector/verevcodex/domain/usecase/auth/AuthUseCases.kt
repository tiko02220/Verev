package com.vector.verevcodex.domain.usecase.auth

import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import javax.inject.Inject

class ObserveSessionUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeSession()
}

class ObserveCurrentSecurityConfigUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeCurrentSecurityConfig()
}

class ObserveEmailNotificationSettingsUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeEmailNotificationSettings()
}

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String) = repository.login(email, password)
}

class RegisterBusinessUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(business: BusinessRegistration, account: AccountRegistration) = 
        repository.registerBusiness(business, account)
}

class SaveSecuritySetupUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(setup: SecuritySetup) = repository.saveSecuritySetup(setup)
}

class UpdateCurrentProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(update: PersonalInformationUpdate) = repository.updateCurrentProfile(update)
}

class ChangeCurrentPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String) =
        repository.changeCurrentPassword(currentPassword, newPassword)
}

class UpdateCurrentQuickPinUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPin: String, newPin: String) =
        repository.updateCurrentQuickPin(currentPin, newPin)
}

class UpdateCurrentBiometricEnabledUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateCurrentBiometricEnabled(enabled)
}

class UpdateEmailNotificationSettingsUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(settings: EmailNotificationSettings) =
        repository.updateEmailNotificationSettings(settings)
}

class VerifyQuickPinUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(pin: String) = repository.verifyQuickPin(pin)
}

class SendPasswordResetCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String) = repository.sendPasswordResetCode(email)
}

class VerifyPasswordResetCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String) = repository.verifyPasswordResetCode(email, code)
}

class ResetPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, newPassword: String) = repository.resetPassword(email, newPassword)
}

class ResetQuickPinUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, newPin: String) = repository.resetQuickPin(email, newPin)
}

class ActivateSessionUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(accountId: String) = repository.activateSession(accountId)
}

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
