package com.vector.verevcodex.data.repository.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.MutablePreferences
import com.google.gson.Gson
import com.vector.verevcodex.common.errors.AppStateException
import com.vector.verevcodex.data.preferences.AccountPreferenceKeys
import com.vector.verevcodex.data.preferences.AuthPreferenceKeys
import com.vector.verevcodex.data.remote.auth.AuthRemoteDataSource
import com.vector.verevcodex.data.remote.auth.TokenStore
import com.vector.verevcodex.data.remote.core.RemoteException
import com.vector.verevcodex.data.preferences.authPreferenceStore
import com.vector.verevcodex.data.preferences.merchantPreferenceStore
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.auth.AccountRegistration
import com.vector.verevcodex.domain.model.auth.AuthBootstrapState
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.AuthUser
import com.vector.verevcodex.domain.model.auth.BusinessRegistration
import com.vector.verevcodex.domain.model.auth.EmailNotificationSettings
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.model.auth.SignupOnboardingProgress
import com.vector.verevcodex.domain.model.auth.SignupOnboardingStage
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson,
    private val authRemote: AuthRemoteDataSource,
    private val tokenStore: TokenStore,
) : AuthRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = context.authPreferenceStore
    private val merchantPreferenceStore = context.merchantPreferenceStore

    init {
        repositoryScope.launch {
            if (tokenStore.hasTokens()) {
                authRemote.me()
                    .onSuccess { (session, _) ->
                        persistSession(session)
                        syncBackendPreferences(session.user.id)
                    }
                    .onFailure { error ->
                        val remoteError = error as? RemoteException
                        val shouldClearSession =
                            remoteError?.kind == RemoteException.Kind.Api &&
                                remoteError.httpStatus in setOf(401, 403)
                        if (shouldClearSession) {
                            tokenStore.clearTokens()
                            clearStoredSession()
                        }
                    }
            }
        }
    }

    override fun observeSession(): Flow<AuthSession?> = dataStore.data
        .map { preferences ->
            preferences[AuthPreferenceKeys.pendingSignupSession]
                ?.let(::deserializeSession)
                ?: preferences[AuthPreferenceKeys.currentSession]?.let(::deserializeSession)
        }

    override fun observeAuthBootstrapState(): Flow<AuthBootstrapState> = dataStore.data.map { preferences ->
        val activeSession = preferences[AuthPreferenceKeys.currentSession]?.let(::deserializeSession)
        val pendingSignupSession = preferences[AuthPreferenceKeys.pendingSignupSession]?.let(::deserializeSession)
        val session = pendingSignupSession ?: activeSession
        val accountId = session.accountIdOrNull() ?: preferences[AuthPreferenceKeys.currentAccountId]
        val securityConfig = accountId?.let {
            val pin = preferences[AccountPreferenceKeys.quickPin(accountId)].orEmpty()
            val hasQuickPin = preferences[AccountPreferenceKeys.quickPinConfigured(accountId)] ?: pin.isNotBlank()
            if (!hasQuickPin) return@let null
            val biometricEnabled = preferences[AccountPreferenceKeys.biometricEnabled(accountId)] ?: false
            SecurityConfig(
                accountId = accountId,
                pin = pin,
                biometricEnabled = biometricEnabled,
                hasQuickPin = hasQuickPin,
            )
        }
        val pendingAccountId = preferences[AuthPreferenceKeys.signupOnboardingPendingAccountId]
        val completedAccountId = preferences[AuthPreferenceKeys.signupOnboardingCompletedAccountId]
        val legacyPending = preferences[AuthPreferenceKeys.signupOnboardingPending] ?: false
        val signupOnboardingPending = resolveSignupOnboardingPending(
            accountId = accountId,
            pendingAccountId = pendingAccountId,
            completedAccountId = completedAccountId,
            legacyPending = legacyPending,
        )
        val signupProgress = if (signupOnboardingPending && accountId != null) {
            preferences[AuthPreferenceKeys.signupOnboardingStage]
                ?.let(::deserializeSignupOnboardingStage)
                ?.let { stage ->
                    SignupOnboardingProgress(
                        accountId = accountId,
                        storeId = preferences[AuthPreferenceKeys.signupOnboardingStoreId],
                        stage = stage,
                        pinSetupSkipped = preferences[AuthPreferenceKeys.signupOnboardingPinSkipped] ?: false,
                    )
                }
        } else {
            null
        }
        AuthBootstrapState(
            session = session,
            securityConfig = securityConfig,
            signupOnboardingPending = signupOnboardingPending,
            signupProgress = signupProgress,
        )
    }

    override fun observeCurrentSecurityConfig(): Flow<SecurityConfig?> = dataStore.data.map { preferences ->
        val accountId = preferences[AuthPreferenceKeys.pendingSignupSession]
            ?.let(::deserializeSession)
            .accountIdOrNull()
            ?: preferences[AuthPreferenceKeys.currentSession]
                ?.let(::deserializeSession)
                .accountIdOrNull()
            ?: preferences[AuthPreferenceKeys.currentAccountId]
            ?: return@map null
        val pin = preferences[AccountPreferenceKeys.quickPin(accountId)].orEmpty()
        val hasQuickPin = preferences[AccountPreferenceKeys.quickPinConfigured(accountId)] ?: pin.isNotBlank()
        if (!hasQuickPin) return@map null
        val biometricEnabled = preferences[AccountPreferenceKeys.biometricEnabled(accountId)] ?: false
        SecurityConfig(accountId = accountId, pin = pin, biometricEnabled = biometricEnabled, hasQuickPin = hasQuickPin)
    }

    override fun observeEmailNotificationSettings(): Flow<EmailNotificationSettings?> = dataStore.data.map { preferences ->
        val accountId = preferences[AuthPreferenceKeys.pendingSignupSession]
            ?.let(::deserializeSession)
            .accountIdOrNull()
            ?: preferences[AuthPreferenceKeys.currentSession]
                ?.let(::deserializeSession)
                .accountIdOrNull()
            ?: preferences[AuthPreferenceKeys.currentAccountId]
            ?: return@map null
        EmailNotificationSettings(
            emailEnabled = preferences[AccountPreferenceKeys.emailEnabled(accountId)] ?: true,
            pushEnabled = preferences[AccountPreferenceKeys.pushEnabled(accountId)] ?: true,
            soundEnabled = preferences[AccountPreferenceKeys.soundEnabled(accountId)] ?: true,
            transactionEmails = preferences[AccountPreferenceKeys.transactionEmails(accountId)] ?: true,
            dailyBusinessSummary = preferences[AccountPreferenceKeys.dailyBusinessSummary(accountId)] ?: true,
            weeklyBusinessSummary = preferences[AccountPreferenceKeys.weeklyBusinessSummary(accountId)]
                ?: preferences[AccountPreferenceKeys.legacyWeeklyBusinessSummary(accountId)]
                ?: true,
            marketingEmails = preferences[AccountPreferenceKeys.marketingEmails(accountId)]
                ?: preferences[AccountPreferenceKeys.legacyMarketingEmails(accountId)]
                ?: false,
            newCustomerPush = preferences[AccountPreferenceKeys.newCustomerPush(accountId)] ?: true,
            transactionPush = preferences[AccountPreferenceKeys.transactionPush(accountId)] ?: true,
            rewardRedeemedPush = preferences[AccountPreferenceKeys.rewardRedeemedPush(accountId)]
                ?: preferences[AccountPreferenceKeys.legacyRewardRedeemedPush(accountId)]
                ?: true,
            programUpdatesPush = preferences[AccountPreferenceKeys.programUpdatesPush(accountId)] ?: true,
            staffActivityPush = preferences[AccountPreferenceKeys.staffActivityPush(accountId)] ?: false,
            systemAlertsPush = preferences[AccountPreferenceKeys.systemAlertsPush(accountId)]
                ?: preferences[AccountPreferenceKeys.legacySystemAlertsPush(accountId)]
                ?: true,
        )
    }

    override fun observeSignupOnboardingPending(): Flow<Boolean> = dataStore.data
        .map { preferences ->
            val currentAccountId = preferences[AuthPreferenceKeys.pendingSignupSession]
                ?.let(::deserializeSession)
                .accountIdOrNull()
                ?: preferences[AuthPreferenceKeys.currentSession]
                    ?.let(::deserializeSession)
                    .accountIdOrNull()
                ?: preferences[AuthPreferenceKeys.currentAccountId]
                ?: return@map false
            val pendingAccountId = preferences[AuthPreferenceKeys.signupOnboardingPendingAccountId]
            val completedAccountId = preferences[AuthPreferenceKeys.signupOnboardingCompletedAccountId]
            val legacyPending = preferences[AuthPreferenceKeys.signupOnboardingPending] ?: false
            resolveSignupOnboardingPending(
                accountId = currentAccountId,
                pendingAccountId = pendingAccountId,
                completedAccountId = completedAccountId,
                legacyPending = legacyPending,
            )
        }

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        return authRemote.loginByEmail(email.trim(), password)
            .fold(
                onSuccess = { (session, _) ->
                    clearPendingSignupSession()
                    persistSessionState(session, signupOnboardingPending = false)
                    syncBackendPreferences(session.user.id)
                    Result.success(session)
                },
                onFailure = { Result.failure(it) },
            )
    }

    override suspend fun registerBusiness(business: BusinessRegistration, account: AccountRegistration): Result<RegistrationResult> {
        val address = "${business.address}, ${business.city} ${business.zipCode}"
        return authRemote.signup(
            organizationLegalName = business.businessName,
            organizationDisplayName = business.businessName,
            industry = business.industry,
            phone = business.phoneNumber,
            organizationEmail = business.businessEmail.trim().lowercase(),
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
            onSuccess = { (result, _) ->
                persistPendingSignupSession(result.session)
                syncBackendPreferences(result.session.user.id)
                Result.success(result)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun saveSecuritySetup(setup: SecuritySetup): Result<Unit> {
        return authRemote.setupSecurity(setup).fold(
            onSuccess = {
                it?.let { config ->
                    dataStore.edit { prefs ->
                        prefs[AccountPreferenceKeys.quickPin(config.accountId)] = setup.pin
                        prefs[AccountPreferenceKeys.quickPinConfigured(config.accountId)] = true
                        prefs[AccountPreferenceKeys.biometricEnabled(config.accountId)] = config.biometricEnabled
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
            onSuccess = {
                persistSession(it)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun changeCurrentPassword(currentPassword: String, newPassword: String): Result<Unit> {
        return authRemote.changePassword(currentPassword, newPassword)
    }

    override suspend fun activateInvitedPassword(newPassword: String): Result<Unit> {
        return authRemote.activateInvitedPassword(newPassword).fold(
            onSuccess = { session ->
                persistSession(session)
                syncBackendPreferences(session.user.id)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateCurrentQuickPin(currentPin: String, newPin: String): Result<Unit> {
        return authRemote.changeQuickPin(currentPin, newPin).fold(
            onSuccess = {
                dataStore.edit { prefs ->
                    currentAccountId()?.let { id ->
                        prefs[AccountPreferenceKeys.quickPin(id)] = newPin
                        prefs[AccountPreferenceKeys.quickPinConfigured(id)] = true
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
                        prefs[AccountPreferenceKeys.biometricEnabled(id)] = enabled
                    }
                }
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateEmailNotificationSettings(settings: EmailNotificationSettings): Result<Unit> {
        val account = currentAccount() ?: return Result.failure(AppStateException(AppStateException.Reason.NoActiveAccount))
        dataStore.edit { preferences ->
            preferences[AccountPreferenceKeys.emailEnabled(account.id)] = settings.emailEnabled
            preferences[AccountPreferenceKeys.pushEnabled(account.id)] = settings.pushEnabled
            preferences[AccountPreferenceKeys.soundEnabled(account.id)] = settings.soundEnabled
            preferences[AccountPreferenceKeys.transactionEmails(account.id)] = settings.transactionEmails
            preferences[AccountPreferenceKeys.dailyBusinessSummary(account.id)] = settings.dailyBusinessSummary
            preferences[AccountPreferenceKeys.weeklyBusinessSummary(account.id)] = settings.weeklyBusinessSummary
            preferences[AccountPreferenceKeys.marketingEmails(account.id)] = settings.marketingEmails
            preferences[AccountPreferenceKeys.newCustomerPush(account.id)] = settings.newCustomerPush
            preferences[AccountPreferenceKeys.transactionPush(account.id)] = settings.transactionPush
            preferences[AccountPreferenceKeys.rewardRedeemedPush(account.id)] = settings.rewardRedeemedPush
            preferences[AccountPreferenceKeys.programUpdatesPush(account.id)] = settings.programUpdatesPush
            preferences[AccountPreferenceKeys.staffActivityPush(account.id)] = settings.staffActivityPush
            preferences[AccountPreferenceKeys.systemAlertsPush(account.id)] = settings.systemAlertsPush
        }
        return Result.success(Unit)
    }

    override suspend fun verifyQuickPin(pin: String): Boolean {
        return authRemote.verifyQuickPin(pin).getOrElse { false }
    }

    override suspend fun sendPasswordResetCode(email: String): Result<Unit> {
        return authRemote.requestPasswordResetByEmail(email.trim().lowercase())
    }

    override suspend fun sendQuickPinResetCode(email: String): Result<Unit> {
        return authRemote.requestQuickPinResetByEmail(email.trim().lowercase())
    }

    override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> {
        return authRemote.verifyPasswordResetByEmail(email.trim().lowercase(), code)
    }

    override suspend fun verifyQuickPinResetCode(email: String, code: String): Result<Unit> {
        return authRemote.verifyQuickPinResetByEmail(email.trim().lowercase(), code)
    }

    override suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        return authRemote.confirmPasswordResetByEmail(email.trim().lowercase(), newPassword).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun resetQuickPin(email: String, newPin: String): Result<Unit> {
        return authRemote.confirmQuickPinResetByEmail(email.trim().lowercase(), newPin).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun activateSession(accountId: String): Result<Unit> {
        val session = currentBootstrapSession()
            ?: return Result.failure(AppStateException(AppStateException.Reason.NoActiveSession))
        return if (session.user.id == accountId) Result.success(Unit)
        else Result.failure(IllegalArgumentException("Invalid account"))
    }

    override suspend fun setSignupOnboardingPending(pending: Boolean) {
        dataStore.edit { preferences ->
            val currentSession = preferences[AuthPreferenceKeys.currentSession]
                ?.let(::deserializeSession)
            val pendingSignupSession = preferences[AuthPreferenceKeys.pendingSignupSession]
                ?.let(::deserializeSession)
            val currentAccountId = pendingSignupSession
                .accountIdOrNull()
                ?: currentSession.accountIdOrNull()
                ?: preferences[AuthPreferenceKeys.currentAccountId]
            if (!pending) {
                val sessionToPromote = pendingSignupSession ?: currentSession
                if (sessionToPromote != null) {
                    preferences[AuthPreferenceKeys.currentAccountId] = sessionToPromote.user.id
                    preferences[AuthPreferenceKeys.currentSession] = gson.toJson(sessionToPromote)
                }
                preferences.remove(AuthPreferenceKeys.pendingSignupSession)
                clearSignupOnboardingProgress(preferences)
            } else {
                val sessionToStage = pendingSignupSession ?: currentSession
                if (sessionToStage != null) {
                    preferences[AuthPreferenceKeys.currentAccountId] = sessionToStage.user.id
                    preferences[AuthPreferenceKeys.pendingSignupSession] = gson.toJson(sessionToStage)
                    preferences.remove(AuthPreferenceKeys.currentSession)
                }
            }
            applySignupOnboardingState(
                preferences = preferences,
                accountId = currentAccountId,
                pending = pending,
            )
        }
    }

    override suspend fun updateSignupOnboardingProgress(progress: SignupOnboardingProgress?) {
        dataStore.edit { preferences ->
            if (progress == null) {
                clearSignupOnboardingProgress(preferences)
                return@edit
            }
            preferences[AuthPreferenceKeys.signupOnboardingStage] = progress.stage.name
            if (!progress.storeId.isNullOrBlank()) {
                preferences[AuthPreferenceKeys.signupOnboardingStoreId] = progress.storeId
            } else {
                preferences.remove(AuthPreferenceKeys.signupOnboardingStoreId)
            }
            preferences[AuthPreferenceKeys.signupOnboardingPinSkipped] = progress.pinSetupSkipped
        }
    }

    override suspend fun logout() {
        authRemote.logout()
        clearStoredSession()
    }

    private suspend fun persistSession(session: AuthSession) {
        dataStore.edit { preferences ->
            val resolvedSession = session.withFallbackCurrency(existingCurrencyFrom(preferences))
            preferences[AuthPreferenceKeys.currentAccountId] = resolvedSession.user.id
            if (preferences[AuthPreferenceKeys.pendingSignupSession] != null) {
                preferences[AuthPreferenceKeys.pendingSignupSession] = gson.toJson(resolvedSession)
            } else {
                preferences[AuthPreferenceKeys.currentSession] = gson.toJson(resolvedSession)
            }
        }
    }

    private suspend fun persistPendingSignupSession(session: AuthSession) {
        dataStore.edit { preferences ->
            val resolvedSession = session.withFallbackCurrency(existingCurrencyFrom(preferences))
            preferences[AuthPreferenceKeys.currentAccountId] = resolvedSession.user.id
            preferences[AuthPreferenceKeys.pendingSignupSession] = gson.toJson(resolvedSession)
            preferences.remove(AuthPreferenceKeys.currentSession)
            applySignupOnboardingState(
                preferences = preferences,
                accountId = resolvedSession.user.id,
                pending = true,
            )
        }
    }

    private suspend fun persistSessionState(session: AuthSession, signupOnboardingPending: Boolean) {
        dataStore.edit { preferences ->
            val resolvedSession = session.withFallbackCurrency(existingCurrencyFrom(preferences))
            preferences[AuthPreferenceKeys.currentAccountId] = resolvedSession.user.id
            preferences[AuthPreferenceKeys.currentSession] = gson.toJson(resolvedSession)
            applySignupOnboardingState(
                preferences = preferences,
                accountId = resolvedSession.user.id,
                pending = signupOnboardingPending,
            )
        }
    }

    private fun applySignupOnboardingState(
        preferences: MutablePreferences,
        accountId: String?,
        pending: Boolean,
    ) {
        preferences[AuthPreferenceKeys.signupOnboardingPending] = pending
        if (pending) {
            if (accountId != null) {
                preferences[AuthPreferenceKeys.signupOnboardingPendingAccountId] = accountId
            } else {
                preferences.remove(AuthPreferenceKeys.signupOnboardingPendingAccountId)
            }
            preferences.remove(AuthPreferenceKeys.signupOnboardingCompletedAccountId)
            return
        }

        preferences.remove(AuthPreferenceKeys.signupOnboardingPendingAccountId)
        if (accountId != null) {
            preferences[AuthPreferenceKeys.signupOnboardingCompletedAccountId] = accountId
        } else {
            preferences.remove(AuthPreferenceKeys.signupOnboardingCompletedAccountId)
        }
    }

    private suspend fun syncBackendPreferences(accountId: String) {
        val snapshot = authRemote.getPreferenceSnapshot().getOrNull() ?: return
        dataStore.edit { prefs ->
            prefs[AccountPreferenceKeys.quickPinConfigured(accountId)] = snapshot.quickPinConfigured
            prefs[AccountPreferenceKeys.biometricEnabled(accountId)] = snapshot.biometricEnabled
            if (!snapshot.quickPinConfigured) {
                prefs.remove(AccountPreferenceKeys.quickPin(accountId))
            }
        }
        snapshot.selectedStoreId?.let { selectedStoreId ->
            merchantPreferenceStore.edit { prefs ->
                prefs[AccountPreferenceKeys.selectedStoreId(accountId)] = selectedStoreId
            }
        }
    }

    private suspend fun currentAccountId(): String? = dataStore.data.first()[AuthPreferenceKeys.currentAccountId]

    private suspend fun currentBootstrapSession(): AuthSession? = dataStore.data.first().let { preferences ->
        preferences[AuthPreferenceKeys.pendingSignupSession]
            ?.let(::deserializeSession)
            ?: preferences[AuthPreferenceKeys.currentSession]?.let(::deserializeSession)
    }

    private suspend fun currentAccount(): AuthUser? {
        val accountId = currentAccountId() ?: return null
        val session = observeSession().first() ?: return null
        return session.user.takeIf { it.id == accountId }
    }

    private suspend fun clearStoredSession() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferenceKeys.currentAccountId)
            preferences.remove(AuthPreferenceKeys.currentSession)
            preferences.remove(AuthPreferenceKeys.pendingSignupSession)
            preferences.remove(AuthPreferenceKeys.signupOnboardingPending)
            preferences.remove(AuthPreferenceKeys.signupOnboardingPendingAccountId)
            preferences.remove(AuthPreferenceKeys.signupOnboardingCompletedAccountId)
            clearSignupOnboardingProgress(preferences)
        }
    }

    private suspend fun clearPendingSignupSession() {
        dataStore.edit { preferences ->
            preferences.remove(AuthPreferenceKeys.pendingSignupSession)
        }
    }

    private fun deserializeSession(rawValue: String): AuthSession? =
        runCatching { gson.fromJson(rawValue, AuthSession::class.java) }.getOrNull()

    private fun existingCurrencyFrom(preferences: androidx.datastore.preferences.core.Preferences): String? =
        preferences[AuthPreferenceKeys.pendingSignupSession]
            ?.let(::deserializeSession)
            ?.user
            ?.defaultCurrencyCode
            ?.takeIf { it.isNotBlank() }
            ?: preferences[AuthPreferenceKeys.currentSession]
                ?.let(::deserializeSession)
                ?.user
                ?.defaultCurrencyCode
                ?.takeIf { it.isNotBlank() }

    private fun deserializeSignupOnboardingStage(rawValue: String): SignupOnboardingStage? =
        runCatching { SignupOnboardingStage.valueOf(rawValue) }.getOrNull()

    private fun clearSignupOnboardingProgress(preferences: MutablePreferences) {
        preferences.remove(AuthPreferenceKeys.signupOnboardingStage)
        preferences.remove(AuthPreferenceKeys.signupOnboardingStoreId)
        preferences.remove(AuthPreferenceKeys.signupOnboardingPinSkipped)
    }
}

internal fun resolveSignupOnboardingPending(
    accountId: String?,
    pendingAccountId: String?,
    completedAccountId: String?,
    legacyPending: Boolean,
): Boolean = when {
    accountId == null -> false
    pendingAccountId != null -> pendingAccountId == accountId && completedAccountId != accountId
    else -> legacyPending && completedAccountId != accountId
}

private fun AuthSession?.accountIdOrNull(): String? = this?.user?.id?.takeIf { it.isNotBlank() }

private fun AuthSession.withFallbackCurrency(fallbackCurrencyCode: String?): AuthSession {
    if (user.defaultCurrencyCode.isNotBlank()) return this
    val resolvedCurrencyCode = fallbackCurrencyCode?.takeIf { it.isNotBlank() } ?: "AMD"
    return copy(user = user.copy(defaultCurrencyCode = resolvedCurrencyCode))
}
