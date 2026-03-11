package com.vector.verevcodex.data.repository.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.repository.DatabaseSeeder
import com.vector.verevcodex.data.db.entity.OwnerEntity
import com.vector.verevcodex.data.db.entity.StoreEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.data.mapper.auth.toSession
import com.vector.verevcodex.domain.model.StaffRole
import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    seeder: DatabaseSeeder,
) : AuthRepository {
    private val dataStore = context.authDataStore
    private val currentAccountKey = stringPreferencesKey("current_account_id")
    private var pendingResetEmail: String? = null
    private var pendingResetCode: String? = null

    init {
        runBlocking { seeder.seedIfNeeded() }
    }

    override fun observeSession(): Flow<AuthSession?> = dataStore.data.map { preferences ->
        val accountId = preferences[currentAccountKey] ?: return@map null
        val account = database.authDao().findById(accountId) ?: return@map null
        account.toSession()
    }

    override fun observeCurrentSecurityConfig(): Flow<SecurityConfig?> = dataStore.data.map { preferences ->
        val accountId = preferences[currentAccountKey] ?: return@map null
        val pin = preferences[stringPreferencesKey("${accountId}_quick_pin")] ?: return@map null
        val biometricEnabled = preferences[booleanPreferencesKey("${accountId}_biometric_enabled")] ?: false
        SecurityConfig(accountId = accountId, pin = pin, biometricEnabled = biometricEnabled)
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        val account = database.authDao().findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
        if (!account.active || account.password != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }
        dataStore.edit { preferences ->
            preferences[currentAccountKey] = account.id
        }
        ensureDemoSecurityIfNeeded(account)
        return Result.success(account.toSession())
    }

    override suspend fun registerBusiness(business: BusinessRegistration, account: AccountRegistration): Result<RegistrationResult> {
        if (database.authDao().findByEmail(account.email.trim().lowercase()) != null) {
            return Result.failure(IllegalArgumentException("Email already exists"))
        }

        val ownerId = UUID.randomUUID().toString()
        val storeId = UUID.randomUUID().toString()
        val authId = UUID.randomUUID().toString()
        val nameParts = account.fullName.trim().split(" ", limit = 2)
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.getOrElse(1) { "" }

        database.ownerDao().insertAll(
            listOf(
                OwnerEntity(
                    id = ownerId,
                    firstName = firstName,
                    lastName = lastName,
                    email = account.email.trim().lowercase(),
                    phoneNumber = business.phoneNumber,
                )
            )
        )
        database.storeDao().insertAll(
            listOf(
                StoreEntity(
                    id = storeId,
                    ownerId = ownerId,
                    name = business.businessName,
                    address = "${business.address}, ${business.city} ${business.zipCode}",
                    contactInfo = business.phoneNumber,
                    category = business.industry,
                    workingHours = "09:00 - 18:00",
                    logoUrl = "",
                    primaryColor = "#0C3B2E",
                    secondaryColor = "#FFBA00",
                    active = true,
                )
            )
        )
        val authAccount = AuthAccountEntity(
            id = authId,
            relatedEntityId = ownerId,
            fullName = account.fullName.trim(),
            email = account.email.trim().lowercase(),
            phoneNumber = business.phoneNumber,
            password = account.password,
            role = StaffRole.OWNER.name,
            active = true,
        )
        database.authDao().insert(authAccount)
        return Result.success(RegistrationResult(authAccount.toSession(), ownerId, storeId))
    }

    override suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit> {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("${setup.accountId}_quick_pin")] = setup.pin
            preferences[booleanPreferencesKey("${setup.accountId}_biometric_enabled")] = setup.biometricEnabled
        }
        return Result.success(Unit)
    }

    override suspend fun verifyQuickPin(pin: String): Boolean {
        val preferences = dataStore.data.first()
        val accountId = preferences[currentAccountKey] ?: return false
        val storedPin = preferences[stringPreferencesKey("${accountId}_quick_pin")] ?: return false
        return storedPin == pin
    }

    override suspend fun sendPasswordResetCode(email: String): Result<Unit> {
        val account = database.authDao().findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
        pendingResetEmail = account.email
        pendingResetCode = DEMO_RESET_CODE
        return Result.success(Unit)
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> {
        return if (pendingResetEmail == email.trim().lowercase() && pendingResetCode == code) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Invalid or expired code. Please try again."))
        }
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val account = database.authDao().findByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        if (pendingResetEmail != normalizedEmail || pendingResetCode != DEMO_RESET_CODE) {
            return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        }
        database.authDao().update(account.copy(password = newPassword))
        pendingResetEmail = null
        pendingResetCode = null
        return Result.success(Unit)
    }

    override suspend fun resetQuickPin(email: String, newPin: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val account = database.authDao().findByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        if (pendingResetEmail != normalizedEmail || pendingResetCode != DEMO_RESET_CODE) {
            return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        }
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("${account.id}_quick_pin")] = newPin
        }
        pendingResetEmail = null
        pendingResetCode = null
        return Result.success(Unit)
    }

    override suspend fun activateSession(accountId: String): Result<Unit> {
        val account = database.authDao().findById(accountId)
            ?: return Result.failure(IllegalArgumentException("Invalid account"))
        dataStore.edit { preferences ->
            preferences[currentAccountKey] = account.id
        }
        ensureDemoSecurityIfNeeded(account)
        return Result.success(Unit)
    }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(currentAccountKey)
        }
    }

    private suspend fun ensureDemoSecurityIfNeeded(account: AuthAccountEntity) {
        val defaultPin = when (account.email) {
            "owner@verevcrm.local", "manager@gmail.com", "staff@gmail.com" -> "1234"
            else -> null
        } ?: return
        dataStore.edit { preferences ->
            val pinKey = stringPreferencesKey("${account.id}_quick_pin")
            val bioKey = booleanPreferencesKey("${account.id}_biometric_enabled")
            if (preferences[pinKey] == null) {
                preferences[pinKey] = defaultPin
            }
            if (preferences[bioKey] != true) {
                preferences[bioKey] = true
            }
        }
    }

    private companion object {
        const val DEMO_RESET_CODE = "123456"
    }
}
