package com.vector.verevcodex.domain.repository.auth

import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>
    fun observeCurrentSecurityConfig(): Flow<SecurityConfig?>
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun registerBusiness(business: BusinessRegistration, account: AccountRegistration): Result<RegistrationResult>
    suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit>
    suspend fun verifyQuickPin(pin: String): Boolean
    suspend fun sendPasswordResetCode(email: String): Result<Unit>
    suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit>
    suspend fun resetPassword(email: String, newPassword: String): Result<Unit>
    suspend fun resetQuickPin(email: String, newPin: String): Result<Unit>
    suspend fun activateSession(accountId: String): Result<Unit>
    suspend fun logout()
}
