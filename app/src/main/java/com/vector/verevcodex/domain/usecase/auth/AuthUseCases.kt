package com.vector.verevcodex.domain.usecase.auth

import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.repository.auth.AuthRepository

class ObserveSessionUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeSession()
}

class ObserveCurrentSecurityConfigUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.observeCurrentSecurityConfig()
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
