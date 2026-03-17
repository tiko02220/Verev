package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.auth.*
import com.vector.verevcodex.data.remote.core.requireRemoteValue
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.data.remote.core.unwrapNullable
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import javax.inject.Inject
import javax.inject.Singleton

data class LoginResult(val session: AuthSession, val syncData: BackendAuthSyncData)
data class SignupResult(val result: RegistrationResult, val syncData: BackendAuthSyncData)
data class MeResult(val session: AuthSession, val syncData: BackendAuthSyncData)
data class MerchantPreferenceSnapshot(
    val accountId: String,
    val selectedStoreId: String?,
    val biometricEnabled: Boolean,
    val quickPinConfigured: Boolean,
)

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val api: VerevAuthApi,
    private val tokenStore: TokenStore,
) {

    suspend fun loginByEmail(email: String, password: String): Result<LoginResult> = runCatching {
        val req = EmailLoginRequestDto(email = email, password = password)
        val response = api.loginByEmail(req)
        val data = response.unwrap { it }
        tokenStore.setTokens(requireRemoteValue(data.accessToken, "Missing access token"), requireRemoteValue(data.refreshToken, "Missing refresh token"))
        val user = requireRemoteValue(data.user, "Missing user")
        val organization = requireRemoteValue(data.organization, "Missing organization")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        val session = user.toAuthSession()
        val syncData = BackendAuthSyncData(
            ownerId = user.id.orEmpty(),
            ownerFullName = user.fullName.orEmpty(),
            ownerEmail = user.email.orEmpty(),
            ownerPhone = user.phoneNumber.orEmpty(),
            organizationDisplayName = organization.displayName.orEmpty(),
            accessibleStoreIds = tenantScope.accessibleStoreIds.orEmpty(),
            defaultStore = null,
        )
        LoginResult(session, syncData)
    }

    suspend fun signup(
        organizationLegalName: String,
        organizationDisplayName: String,
        industry: String,
        phone: String,
        ownerEmail: String,
        ownerFullName: String,
        ownerPhoneNumber: String,
        password: String,
        storeName: String,
        storeAddress: String,
        storeContactInfo: String,
        storeCategory: String,
        storeWorkingHours: String,
        defaultCurrencyCode: String = "USD",
        defaultTimezone: String = "America/New_York",
    ): Result<SignupResult> = runCatching {
        val request = SignupRequestDto(
            organization = OrganizationSignupPayloadDto(
                legalName = organizationLegalName,
                displayName = organizationDisplayName,
                industry = industry,
                phone = phone,
                email = ownerEmail,
                defaultCurrencyCode = defaultCurrencyCode,
                defaultTimezone = defaultTimezone,
            ),
            owner = OwnerSignupPayloadDto(
                fullName = ownerFullName,
                email = ownerEmail,
                phoneNumber = ownerPhoneNumber,
                password = password,
            ),
            store = StoreSignupPayloadDto(
                name = storeName,
                address = storeAddress,
                contactInfo = storeContactInfo,
                category = storeCategory,
                workingHours = storeWorkingHours,
            ),
        )
        val response = api.signup(request)
        val data = response.unwrap { it }
        val user = requireRemoteValue(data.user, "Missing user")
        val organization = requireRemoteValue(data.organization, "Missing organization")
        val defaultStore = requireRemoteValue(data.defaultStore, "Missing default store")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        tokenStore.setTokens(requireRemoteValue(data.accessToken, "Missing access token"), requireRemoteValue(data.refreshToken, "Missing refresh token"))
        val result = RegistrationResult(
            session = user.toAuthSession(),
            businessId = organization.id.orEmpty(),
            defaultStoreId = defaultStore.id.orEmpty(),
        )
        val syncData = BackendAuthSyncData(
            ownerId = user.id.orEmpty(),
            ownerFullName = user.fullName.orEmpty(),
            ownerEmail = user.email.orEmpty(),
            ownerPhone = user.phoneNumber.orEmpty(),
            organizationDisplayName = organization.displayName.orEmpty(),
            accessibleStoreIds = tenantScope.accessibleStoreIds.orEmpty(),
            defaultStore = defaultStore,
        )
        SignupResult(result, syncData)
    }

    suspend fun refresh(): Result<AuthSession?> = runCatching {
        val refreshToken = tokenStore.getRefreshToken() ?: return@runCatching null
        val response = api.refresh(RefreshRequestDto(refreshToken = refreshToken))
        val data = response.unwrap { it }
        tokenStore.setTokens(requireRemoteValue(data.accessToken, "Missing access token"), data.refreshToken ?: refreshToken)
        null
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken != null) {
            api.logout(LogoutRequestDto(refreshToken = refreshToken))
        }
        tokenStore.clearTokens()
        Unit
    }

    suspend fun me(): Result<MeResult> = runCatching {
        val data = api.me().unwrap { it }
        val user = requireRemoteValue(data.user, "Missing user")
        val organization = requireRemoteValue(data.organization, "Missing organization")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        val session = user.toAuthSession()
        val syncData = BackendAuthSyncData(
            ownerId = user.id.orEmpty(),
            ownerFullName = user.fullName.orEmpty(),
            ownerEmail = user.email.orEmpty(),
            ownerPhone = user.phoneNumber.orEmpty(),
            organizationDisplayName = organization.displayName.orEmpty(),
            accessibleStoreIds = tenantScope.accessibleStoreIds.orEmpty(),
            defaultStore = null,
        )
        MeResult(session, syncData)
    }

    suspend fun updateMe(update: PersonalInformationUpdate): Result<AuthSession> = runCatching {
        val req = UpdateMeRequestDto(
            fullName = update.fullName,
            email = update.email,
            phoneNumber = update.phoneNumber,
            profilePhotoUri = update.profilePhotoUri,
        )
        val data = api.updateMe(req).unwrap { it }
        requireRemoteValue(data.user, "Missing user").toAuthSession()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val req = ChangePasswordRequestDto(currentPassword = currentPassword, newPassword = newPassword)
        api.changePassword(req)
        Unit
    }

    suspend fun getSecurityPreferences(): Result<SecurityConfig?> = runCatching {
        val response = api.securityPreferences()
        response.unwrapNullable { data ->
            SecurityConfig(
                accountId = data.userId.orEmpty(),
                pin = "", // PIN is never returned by API
                biometricEnabled = data.biometricEnabled ?: false,
            )
        }
    }

    suspend fun getPreferenceSnapshot(): Result<MerchantPreferenceSnapshot?> = runCatching {
        api.securityPreferences().unwrapNullable { data ->
            MerchantPreferenceSnapshot(
                accountId = data.userId.orEmpty(),
                selectedStoreId = data.selectedStoreId,
                biometricEnabled = data.biometricEnabled ?: false,
                quickPinConfigured = data.quickPinConfigured ?: false,
            )
        }
    }

    suspend fun setupSecurity(setup: SecuritySetup): Result<SecurityConfig?> = runCatching {
        val req = SecuritySetupRequestDto(
            pin = setup.pin,
            biometricEnabled = setup.biometricEnabled,
            selectedStoreId = null,
        )
        api.setupSecurity(req).unwrapNullable { data ->
            SecurityConfig(
                accountId = data.userId.orEmpty(),
                pin = setup.pin,
                biometricEnabled = data.biometricEnabled ?: false,
            )
        }
    }

    suspend fun verifyQuickPin(pin: String): Result<Boolean> = runCatching {
        val response = api.verifyQuickPin(VerifyQuickPinRequestDto(pin = pin))
        response.unwrap { it.valid ?: false }
    }

    suspend fun changeQuickPin(currentPin: String, newPin: String): Result<Unit> = runCatching {
        val req = ChangeQuickPinRequestDto(currentPin = currentPin, newPin = newPin)
        api.changeQuickPin(req).unwrap { Unit }
    }

    suspend fun updateBiometric(enabled: Boolean): Result<Unit> = runCatching {
        api.updateBiometric(UpdateBiometricPreferenceRequestDto(enabled = enabled)).unwrap { Unit }
    }

    suspend fun updateSelectedStore(storeId: String): Result<Unit> = runCatching {
        api.updateSelectedStore(UpdateSelectedStoreRequestDto(storeId = storeId)).unwrap { Unit }
    }

    suspend fun requestPasswordResetByEmail(email: String): Result<Unit> = runCatching {
        api.passwordResetRequestByEmail(EmailPasswordResetRequestDto(email = email, channel = "EMAIL")).unwrap { Unit }
    }

    suspend fun requestQuickPinResetByEmail(email: String): Result<Unit> = runCatching {
        api.quickPinResetRequestByEmail(EmailQuickPinResetRequestDto(email = email, channel = "EMAIL")).unwrap { Unit }
    }

    suspend fun verifyPasswordResetByEmail(email: String, code: String): Result<Unit> = runCatching {
        api.passwordResetVerifyByEmail(EmailPasswordResetVerifyRequestDto(email = email, code = code)).unwrap { Unit }
    }

    suspend fun verifyQuickPinResetByEmail(email: String, code: String): Result<Unit> = runCatching {
        api.quickPinResetVerifyByEmail(EmailQuickPinResetVerifyRequestDto(email = email, code = code)).unwrap { Unit }
    }

    suspend fun confirmPasswordResetByEmail(email: String, newPassword: String): Result<Unit> = runCatching {
        api.passwordResetConfirmByEmail(EmailPasswordResetConfirmRequestDto(email = email, newPassword = newPassword)).unwrap { Unit }
    }

    suspend fun confirmQuickPinResetByEmail(email: String, newPin: String): Result<Unit> = runCatching {
        api.quickPinResetConfirmByEmail(EmailQuickPinResetConfirmRequestDto(email = email, newPin = newPin)).unwrap { Unit }
    }

}

class ApiException(val code: Int, override val message: String) : Exception(message)
