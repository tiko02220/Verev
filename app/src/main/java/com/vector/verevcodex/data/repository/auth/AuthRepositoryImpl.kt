package com.vector.verevcodex.data.repository.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.DatabaseSeeder
import com.vector.verevcodex.data.repository.settings.BusinessSettingsRepositoryImpl
import com.vector.verevcodex.data.preferences.authPreferenceStore
import com.vector.verevcodex.data.db.entity.business.OwnerEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.data.mapper.auth.toSession
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    private val businessSettingsRepository: BusinessSettingsRepositoryImpl,
    seeder: DatabaseSeeder,
) : AuthRepository {
    private val dataStore = context.authPreferenceStore
    private val currentAccountKey = stringPreferencesKey("current_account_id")
    private val resetEmailKey = stringPreferencesKey("pending_reset_email")
    private val resetCodeKey = stringPreferencesKey("pending_reset_code")
    private val resetIssuedAtKey = longPreferencesKey("pending_reset_issued_at")
    private val resetVerifiedEmailKey = stringPreferencesKey("pending_reset_verified_email")

    init {
        runBlocking { seeder.seedIfNeeded() }
    }

    override fun observeSession(): Flow<AuthSession?> = dataStore.data
        .map { preferences -> preferences[currentAccountKey] }
        .flatMapLatest { accountId ->
            if (accountId == null) {
                kotlinx.coroutines.flow.flowOf(null)
            } else {
                database.authDao().observeById(accountId).map { account -> account?.toSession() }
            }
        }

    override fun observeCurrentSecurityConfig(): Flow<SecurityConfig?> = dataStore.data.map { preferences ->
        val accountId = preferences[currentAccountKey] ?: return@map null
        val pin = preferences[stringPreferencesKey("${accountId}_quick_pin")] ?: return@map null
        val biometricEnabled = preferences[booleanPreferencesKey("${accountId}_biometric_enabled")] ?: false
        SecurityConfig(accountId = accountId, pin = pin, biometricEnabled = biometricEnabled)
    }

    override fun observeEmailNotificationSettings(): Flow<EmailNotificationSettings?> = dataStore.data.map { preferences ->
        val accountId = preferences[currentAccountKey] ?: return@map null
        EmailNotificationSettings(
            emailEnabled = preferences[booleanPreferencesKey("${accountId}_notify_email_enabled")] ?: true,
            pushEnabled = preferences[booleanPreferencesKey("${accountId}_notify_push_enabled")] ?: true,
            soundEnabled = preferences[booleanPreferencesKey("${accountId}_notify_sound_enabled")] ?: true,
            transactionEmails = preferences[booleanPreferencesKey("${accountId}_notify_email_transactions")] ?: true,
            dailyBusinessSummary = preferences[booleanPreferencesKey("${accountId}_notify_email_daily_summary")] ?: true,
            weeklyBusinessSummary = preferences[booleanPreferencesKey("${accountId}_notify_email_weekly_summary")]
                ?: preferences[booleanPreferencesKey("${accountId}_notify_summary")]
                ?: true,
            marketingEmails = preferences[booleanPreferencesKey("${accountId}_notify_email_marketing")]
                ?: preferences[booleanPreferencesKey("${accountId}_notify_promotions")]
                ?: false,
            newCustomerPush = preferences[booleanPreferencesKey("${accountId}_notify_push_new_customer")] ?: true,
            transactionPush = preferences[booleanPreferencesKey("${accountId}_notify_push_transactions")] ?: true,
            rewardRedeemedPush = preferences[booleanPreferencesKey("${accountId}_notify_push_reward_redeemed")]
                ?: preferences[booleanPreferencesKey("${accountId}_notify_loyalty")]
                ?: true,
            programUpdatesPush = preferences[booleanPreferencesKey("${accountId}_notify_push_program_updates")] ?: true,
            staffActivityPush = preferences[booleanPreferencesKey("${accountId}_notify_push_staff_activity")] ?: false,
            systemAlertsPush = preferences[booleanPreferencesKey("${accountId}_notify_push_system_alerts")]
                ?: preferences[booleanPreferencesKey("${accountId}_notify_security")]
                ?: true,
        )
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
        businessSettingsRepository.createDefaultStoreSettings(
            storeId = storeId,
            ownerId = ownerId,
            primaryColor = "#0C3B2E",
            secondaryColor = "#FFBA00",
        )
        return Result.success(RegistrationResult(authAccount.toSession(), ownerId, storeId))
    }

    override suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit> {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("${setup.accountId}_quick_pin")] = setup.pin
            preferences[booleanPreferencesKey("${setup.accountId}_biometric_enabled")] = setup.biometricEnabled
        }
        return Result.success(Unit)
    }

    override suspend fun updateCurrentProfile(update: PersonalInformationUpdate): Result<Unit> {
        val accountId = currentAccountId() ?: return Result.failure(IllegalArgumentException("No active session"))
        val currentAccount = database.authDao().findById(accountId)
            ?: return Result.failure(IllegalArgumentException("No active account"))
        val normalizedEmail = update.email.trim().lowercase()
        val existing = database.authDao().findByEmail(normalizedEmail)
        if (existing != null && existing.id != currentAccount.id) {
            return Result.failure(IllegalArgumentException("Email already exists"))
        }
        database.authDao().update(
            currentAccount.copy(
                fullName = update.fullName.trim(),
                email = normalizedEmail,
                phoneNumber = update.phoneNumber.trim(),
            )
        )
        return Result.success(Unit)
    }

    override suspend fun changeCurrentPassword(currentPassword: String, newPassword: String): Result<Unit> {
        val account = currentAccount() ?: return Result.failure(IllegalArgumentException("No active account"))
        if (account.password != currentPassword) {
            return Result.failure(IllegalArgumentException("Current password is incorrect"))
        }
        database.authDao().update(account.copy(password = newPassword))
        return Result.success(Unit)
    }

    override suspend fun updateCurrentQuickPin(currentPin: String, newPin: String): Result<Unit> {
        val account = currentAccount() ?: return Result.failure(IllegalArgumentException("No active account"))
        val preferences = dataStore.data.first()
        val pinKey = stringPreferencesKey("${account.id}_quick_pin")
        val storedPin = preferences[pinKey]
        if (storedPin != null && storedPin != currentPin) {
            return Result.failure(IllegalArgumentException("Current PIN is incorrect"))
        }
        dataStore.edit { mutablePreferences ->
            mutablePreferences[pinKey] = newPin
        }
        return Result.success(Unit)
    }

    override suspend fun updateCurrentBiometricEnabled(enabled: Boolean): Result<Unit> {
        val account = currentAccount() ?: return Result.failure(IllegalArgumentException("No active account"))
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("${account.id}_biometric_enabled")] = enabled
        }
        return Result.success(Unit)
    }

    override suspend fun updateEmailNotificationSettings(settings: EmailNotificationSettings): Result<Unit> {
        val account = currentAccount() ?: return Result.failure(IllegalArgumentException("No active account"))
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("${account.id}_notify_email_enabled")] = settings.emailEnabled
            preferences[booleanPreferencesKey("${account.id}_notify_push_enabled")] = settings.pushEnabled
            preferences[booleanPreferencesKey("${account.id}_notify_sound_enabled")] = settings.soundEnabled
            preferences[booleanPreferencesKey("${account.id}_notify_email_transactions")] = settings.transactionEmails
            preferences[booleanPreferencesKey("${account.id}_notify_email_daily_summary")] = settings.dailyBusinessSummary
            preferences[booleanPreferencesKey("${account.id}_notify_email_weekly_summary")] = settings.weeklyBusinessSummary
            preferences[booleanPreferencesKey("${account.id}_notify_email_marketing")] = settings.marketingEmails
            preferences[booleanPreferencesKey("${account.id}_notify_push_new_customer")] = settings.newCustomerPush
            preferences[booleanPreferencesKey("${account.id}_notify_push_transactions")] = settings.transactionPush
            preferences[booleanPreferencesKey("${account.id}_notify_push_reward_redeemed")] = settings.rewardRedeemedPush
            preferences[booleanPreferencesKey("${account.id}_notify_push_program_updates")] = settings.programUpdatesPush
            preferences[booleanPreferencesKey("${account.id}_notify_push_staff_activity")] = settings.staffActivityPush
            preferences[booleanPreferencesKey("${account.id}_notify_push_system_alerts")] = settings.systemAlertsPush

            // Keep legacy keys in sync while the local app still reads older preferences in some flows.
            preferences[booleanPreferencesKey("${account.id}_notify_promotions")] = settings.marketingEmails
            preferences[booleanPreferencesKey("${account.id}_notify_loyalty")] = settings.rewardRedeemedPush
            preferences[booleanPreferencesKey("${account.id}_notify_summary")] = settings.weeklyBusinessSummary
            preferences[booleanPreferencesKey("${account.id}_notify_security")] = settings.systemAlertsPush
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
        val code = PasswordResetCodePolicy.generate()
        val issuedAt = System.currentTimeMillis()
        dataStore.edit { preferences ->
            preferences[resetEmailKey] = account.email
            preferences[resetCodeKey] = code
            preferences[resetIssuedAtKey] = issuedAt
            preferences.remove(resetVerifiedEmailKey)
        }
        return Result.success(Unit)
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val preferences = dataStore.data.first()
        val storedEmail = preferences[resetEmailKey]
        val storedCode = preferences[resetCodeKey]
        val issuedAt = preferences[resetIssuedAtKey] ?: 0L
        val isValid = storedEmail == normalizedEmail &&
            storedCode == code &&
            !PasswordResetCodePolicy.isExpired(issuedAt, System.currentTimeMillis())
        return if (isValid) {
            dataStore.edit { mutablePreferences ->
                mutablePreferences[resetVerifiedEmailKey] = normalizedEmail
            }
            Result.success(Unit)
        } else {
            clearResetSession()
            Result.failure(IllegalArgumentException("Invalid or expired code. Please try again."))
        }
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val account = database.authDao().findByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        if (!hasVerifiedResetSession(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        }
        database.authDao().update(account.copy(password = newPassword))
        clearResetSession()
        return Result.success(Unit)
    }

    override suspend fun resetQuickPin(email: String, newPin: String): Result<Unit> {
        val normalizedEmail = email.trim().lowercase()
        val account = database.authDao().findByEmail(normalizedEmail)
            ?: return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        if (!hasVerifiedResetSession(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("Failed to reset password. Please try again."))
        }
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("${account.id}_quick_pin")] = newPin
        }
        clearResetSession()
        return Result.success(Unit)
    }

    override suspend fun activateSession(accountId: String): Result<Unit> {
        val account = database.authDao().findById(accountId)
            ?: return Result.failure(IllegalArgumentException("Invalid account"))
        dataStore.edit { preferences ->
            preferences[currentAccountKey] = account.id
        }
        return Result.success(Unit)
    }

    override suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(currentAccountKey)
        }
    }

    private suspend fun currentAccountId(): String? = dataStore.data.first()[currentAccountKey]

    private suspend fun currentAccount(): AuthAccountEntity? {
        val accountId = currentAccountId() ?: return null
        return database.authDao().findById(accountId)
    }

    private suspend fun hasVerifiedResetSession(email: String): Boolean {
        val preferences = dataStore.data.first()
        val verifiedEmail = preferences[resetVerifiedEmailKey]
        val issuedAt = preferences[resetIssuedAtKey] ?: return false
        return verifiedEmail == email && !PasswordResetCodePolicy.isExpired(issuedAt, System.currentTimeMillis())
    }

    private suspend fun clearResetSession() {
        dataStore.edit { preferences ->
            preferences.remove(resetEmailKey)
            preferences.remove(resetCodeKey)
            preferences.remove(resetIssuedAtKey)
            preferences.remove(resetVerifiedEmailKey)
        }
    }
}
