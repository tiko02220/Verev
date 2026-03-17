package com.vector.verevcodex.data.repository.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.DatabaseSeeder
import com.vector.verevcodex.data.remote.api.auth.StoreViewDto
import com.vector.verevcodex.data.remote.auth.AuthRemoteDataSource
import com.vector.verevcodex.data.remote.auth.BackendAuthSyncData
import com.vector.verevcodex.data.remote.auth.TokenStore
import com.vector.verevcodex.data.repository.settings.BusinessSettingsRepositoryImpl
import com.vector.verevcodex.data.preferences.authPreferenceStore
import com.vector.verevcodex.data.preferences.merchantPreferenceStore
import com.vector.verevcodex.data.db.entity.business.OwnerEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import com.vector.verevcodex.data.mapper.auth.toEntity
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
    private val authRemote: AuthRemoteDataSource,
    private val tokenStore: TokenStore,
    seeder: DatabaseSeeder,
) : AuthRepository {
    private val dataStore = context.authPreferenceStore
    private val merchantPreferenceStore = context.merchantPreferenceStore
    private val currentAccountKey = stringPreferencesKey("current_account_id")
    private val resetEmailKey = stringPreferencesKey("pending_reset_email")
    private val resetCodeKey = stringPreferencesKey("pending_reset_code")
    private val resetIssuedAtKey = longPreferencesKey("pending_reset_issued_at")
    private val resetVerifiedEmailKey = stringPreferencesKey("pending_reset_verified_email")

    init {
        runBlocking {
            seeder.seedIfNeeded()
            if (tokenStore.hasTokens()) {
                authRemote.me()
                    .onSuccess { (session, syncData) ->
                        syncSessionToLocal(session)
                        syncOwnerAndStoreFromBackend(syncData)
                        dataStore.edit { it[currentAccountKey] = session.user.id }
                        syncBackendPreferences(session.user.id)
                    }
                    .onFailure {
                        tokenStore.clearTokens()
                    }
            }
        }
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
        return authRemote.loginByEmail(email.trim(), password)
            .fold(
                onSuccess = { (session, syncData) ->
                    syncSessionToLocal(session)
                    syncOwnerAndStoreFromBackend(syncData)
                    dataStore.edit { it[currentAccountKey] = session.user.id }
                    syncBackendPreferences(session.user.id)
                    Result.success(session)
                },
                onFailure = { Result.failure(friendlyAuthError(it)) },
            )
    }

    override suspend fun registerBusiness(business: BusinessRegistration, account: AccountRegistration): Result<RegistrationResult> {
        val address = "${business.address}, ${business.city} ${business.zipCode}"
        return authRemote.signup(
            organizationLegalName = business.businessName,
            organizationDisplayName = business.businessName,
            industry = business.industry,
            phone = business.phoneNumber,
            ownerEmail = account.email.trim().lowercase(),
            ownerFullName = account.fullName.trim(),
            ownerPhoneNumber = business.phoneNumber,
            password = account.password,
            storeName = business.businessName,
            storeAddress = address,
            storeContactInfo = business.phoneNumber,
            storeCategory = business.industry,
            storeWorkingHours = "09:00 - 18:00",
        ).fold(
            onSuccess = { (result, syncData) ->
                syncSessionToLocal(result.session)
                syncOwnerAndStoreFromBackend(syncData)
                dataStore.edit { it[currentAccountKey] = result.session.user.id }
                syncBackendPreferences(result.session.user.id)
                businessSettingsRepository.createDefaultStoreSettings(
                    storeId = result.defaultStoreId,
                    ownerId = syncData.ownerId,
                    primaryColor = syncData.defaultStore?.primaryColor ?: "#0C3B2E",
                    secondaryColor = syncData.defaultStore?.secondaryColor ?: "#FFBA00",
                )
                Result.success(result)
            },
            onFailure = { Result.failure(friendlyAuthError(it)) },
        )
    }

    override suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit> {
        return authRemote.setupSecurity(setup).fold(
            onSuccess = {
                it?.let { config ->
                    dataStore.edit { prefs ->
                        prefs[stringPreferencesKey("${config.accountId}_quick_pin")] = setup.pin
                        prefs[booleanPreferencesKey("${config.accountId}_biometric_enabled")] = config.biometricEnabled
                    }
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateCurrentProfile(update: PersonalInformationUpdate): Result<Unit> {
        return authRemote.updateMe(
            PersonalInformationUpdate(
                fullName = update.fullName,
                email = update.email,
                phoneNumber = update.phoneNumber,
                profilePhotoUri = update.profilePhotoUri,
            )
        ).fold(
            onSuccess = { syncSessionToLocal(it); Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun changeCurrentPassword(currentPassword: String, newPassword: String): Result<Unit> {
        return authRemote.changePassword(currentPassword, newPassword)
    }

    override suspend fun updateCurrentQuickPin(currentPin: String, newPin: String): Result<Unit> {
        return authRemote.changeQuickPin(currentPin, newPin).fold(
            onSuccess = {
                dataStore.edit { prefs ->
                    currentAccountId()?.let { id ->
                        prefs[stringPreferencesKey("${id}_quick_pin")] = newPin
                    }
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateCurrentBiometricEnabled(enabled: Boolean): Result<Unit> {
        return authRemote.updateBiometric(enabled).fold(
            onSuccess = {
                dataStore.edit { prefs ->
                    currentAccountId()?.let { id ->
                        prefs[booleanPreferencesKey("${id}_biometric_enabled")] = enabled
                    }
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
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
        return authRemote.verifyQuickPin(pin).getOrElse { false }
    }

    override suspend fun sendPasswordResetCode(email: String): Result<Unit> {
        return authRemote.requestPasswordResetByEmail(email.trim().lowercase())
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> {
        return authRemote.verifyPasswordResetByEmail(email.trim().lowercase(), code)
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return authRemote.confirmPasswordResetByEmail(email.trim().lowercase(), newPassword).fold(
            onSuccess = { clearResetSession(); Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun resetQuickPin(email: String, newPin: String): Result<Unit> {
        return authRemote.confirmQuickPinResetByEmail(email.trim().lowercase(), newPin).fold(
            onSuccess = { clearResetSession(); Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
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
        authRemote.logout()
        dataStore.edit { preferences ->
            preferences.remove(currentAccountKey)
        }
    }

    private suspend fun syncSessionToLocal(session: AuthSession) {
        database.authDao().insert(session.user.toEntity(password = ""))
    }

    private suspend fun syncOwnerAndStoreFromBackend(syncData: BackendAuthSyncData) {
        val nameParts = syncData.ownerFullName.trim().split(" ", limit = 2)
        val firstName = nameParts.firstOrNull().orEmpty()
        val lastName = nameParts.getOrElse(1) { "" }
        database.ownerDao().insertAll(
            listOf(
                OwnerEntity(
                    id = syncData.ownerId,
                    firstName = firstName,
                    lastName = lastName,
                    email = syncData.ownerEmail,
                    phoneNumber = syncData.ownerPhone,
                )
            )
        )
        val stores = if (syncData.defaultStore != null) {
            listOf(syncData.defaultStore.toStoreEntity(syncData.ownerId))
        } else {
            syncData.accessibleStoreIds.map { storeId ->
                StoreEntity(
                    id = storeId,
                    ownerId = syncData.ownerId,
                    name = syncData.organizationDisplayName,
                    address = "",
                    contactInfo = syncData.ownerPhone,
                    category = "",
                    workingHours = "",
                    logoUrl = "",
                    primaryColor = "#0C3B2E",
                    secondaryColor = "#FFBA00",
                    active = true,
                )
            }
        }
        if (stores.isNotEmpty()) database.storeDao().insertAll(stores)
    }

    private suspend fun syncBackendPreferences(accountId: String) {
        val snapshot = authRemote.getPreferenceSnapshot().getOrNull() ?: return
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("${accountId}_biometric_enabled")] = snapshot.biometricEnabled
            if (!snapshot.quickPinConfigured) {
                prefs.remove(stringPreferencesKey("${accountId}_quick_pin"))
            }
        }
        snapshot.selectedStoreId?.let { selectedStoreId ->
            merchantPreferenceStore.edit { prefs ->
                prefs[stringPreferencesKey("${accountId}_selected_store_id")] = selectedStoreId
            }
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

    /** Maps network/connection errors to a message that helps when login works in Swagger but not on device. */
    private fun friendlyAuthError(e: Throwable): Exception {
        val cause = e.cause ?: e
        return when (cause) {
            is ConnectException, is UnknownHostException, is SocketTimeoutException ->
                Exception(
                    "Cannot reach server. In local.properties set verev.backend.baseUrl to your computer's IP (e.g. http://192.168.1.x:8080), " +
                        "ensure the phone is on the same Wi‑Fi as the backend, then rebuild the app.",
                    cause,
                )
            else -> if (e is Exception) e else Exception(e.message, e)
        }
    }

    private fun StoreViewDto.toStoreEntity(ownerId: String): StoreEntity = StoreEntity(
        id = id.orEmpty(),
        ownerId = ownerId,
        name = name.orEmpty(),
        address = address.orEmpty(),
        contactInfo = contactInfo.orEmpty(),
        category = category.orEmpty(),
        workingHours = workingHours.orEmpty(),
        logoUrl = logoUrl.orEmpty(),
        primaryColor = primaryColor.orEmpty().ifEmpty { "#0C3B2E" },
        secondaryColor = secondaryColor.orEmpty().ifEmpty { "#FFBA00" },
        active = active ?: false,
    )
}
