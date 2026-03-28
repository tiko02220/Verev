package com.vector.verevcodex.data.remote.auth

import com.vector.verevcodex.common.phone.normalizePhoneNumber
import com.vector.verevcodex.data.remote.api.VerevAuthApi
import com.vector.verevcodex.data.remote.api.auth.*
import com.vector.verevcodex.data.remote.core.requireRemoteValue
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.data.remote.core.unwrapNullable
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.auth.PersonalInformationUpdate
import com.vector.verevcodex.domain.model.auth.RegistrationResult
import com.vector.verevcodex.domain.model.auth.SecurityConfig
import com.vector.verevcodex.domain.model.auth.SecuritySetup
import com.vector.verevcodex.domain.model.common.StaffPermissions
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

    suspend fun loginByEmail(email: String, password: String): Result<LoginResult> = remoteResult {
        val req = EmailLoginRequestDto(email = email, password = password)
        val response = api.loginByEmail(req)
        val data = response.unwrap { it }
        tokenStore.setTokens(requireRemoteValue(data.accessToken, "Missing access token"), requireRemoteValue(data.refreshToken, "Missing refresh token"))
        val user = requireRemoteValue(data.user, "Missing user")
        val organization = requireRemoteValue(data.organization, "Missing organization")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        val session = user.toAuthSession(permissions = tenantScope.permissions.toStaffPermissions())
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
        organizationEmail: String,
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
    ): Result<SignupResult> = remoteResult {
        val normalizedOrganizationPhone = normalizePhoneNumber(phone)
        val normalizedOwnerPhoneNumber = normalizePhoneNumber(ownerPhoneNumber)
        val normalizedStoreContactInfo = normalizePhoneNumber(storeContactInfo)
        val request = SignupRequestDto(
            organization = OrganizationSignupPayloadDto(
                legalName = organizationLegalName,
                displayName = organizationDisplayName,
                industry = industry,
                phone = normalizedOrganizationPhone,
                email = organizationEmail,
                defaultCurrencyCode = defaultCurrencyCode,
                defaultTimezone = defaultTimezone,
            ),
            owner = OwnerSignupPayloadDto(
                fullName = ownerFullName,
                email = ownerEmail,
                phoneNumber = normalizedOwnerPhoneNumber,
                password = password,
            ),
            store = StoreSignupPayloadDto(
                name = storeName,
                address = storeAddress,
                contactInfo = normalizedStoreContactInfo,
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
            session = user.toAuthSession(permissions = tenantScope.permissions.toStaffPermissions()),
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

    suspend fun refresh(): Result<AuthSession?> = remoteResult {
        val refreshToken = tokenStore.getRefreshToken() ?: return@remoteResult null
        val response = api.refresh(RefreshRequestDto(refreshToken = refreshToken))
        val data = response.unwrap { it }
        tokenStore.setTokens(requireRemoteValue(data.accessToken, "Missing access token"), data.refreshToken ?: refreshToken)
        null
    }

    suspend fun logout(): Result<Unit> = remoteResult {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken != null) {
            api.logout(LogoutRequestDto(refreshToken = refreshToken))
        }
        tokenStore.clearTokens()
        Unit
    }

    suspend fun me(): Result<MeResult> = remoteResult {
        val data = api.me().unwrap { it }
        val user = requireRemoteValue(data.user, "Missing user")
        val organization = requireRemoteValue(data.organization, "Missing organization")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        val session = user.toAuthSession(permissions = tenantScope.permissions.toStaffPermissions())
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

    suspend fun updateMe(update: PersonalInformationUpdate): Result<AuthSession> = remoteResult {
        val req = UpdateMeRequestDto(
            fullName = update.fullName,
            email = update.email,
            phoneNumber = normalizePhoneNumber(update.phoneNumber),
            profilePhotoUri = update.profilePhotoUri,
        )
        val data = api.updateMe(req).unwrap { it }
        requireRemoteValue(data.user, "Missing user").toAuthSession()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = remoteResult {
        val req = ChangePasswordRequestDto(currentPassword = currentPassword, newPassword = newPassword)
        api.changePassword(req)
        Unit
    }

    suspend fun activateInvitedPassword(newPassword: String): Result<AuthSession> = remoteResult {
        val data = api.activateInvitedPassword(ActivateInvitedPasswordRequestDto(newPassword = newPassword)).unwrap { it }
        val user = requireRemoteValue(data.user, "Missing user")
        val tenantScope = requireRemoteValue(data.tenantScope, "Missing tenant scope")
        user.toAuthSession(permissions = tenantScope.permissions.toStaffPermissions())
    }

    suspend fun getSecurityPreferences(): Result<SecurityConfig?> = remoteResult {
        val response = api.securityPreferences()
        response.unwrapNullable { data ->
            SecurityConfig(
                accountId = data.userId.orEmpty(),
                pin = "", // PIN is never returned by API
                biometricEnabled = data.biometricEnabled ?: false,
                hasQuickPin = data.quickPinConfigured ?: false,
            )
        }
    }

    suspend fun getPreferenceSnapshot(): Result<MerchantPreferenceSnapshot?> = remoteResult {
        api.securityPreferences().unwrapNullable { data ->
            MerchantPreferenceSnapshot(
                accountId = data.userId.orEmpty(),
                selectedStoreId = data.selectedStoreId,
                biometricEnabled = data.biometricEnabled ?: false,
                quickPinConfigured = data.quickPinConfigured ?: false,
            )
        }
    }

    suspend fun setupSecurity(setup: SecuritySetup): Result<SecurityConfig?> = remoteResult {
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
                hasQuickPin = true,
            )
        }
    }

    suspend fun verifyQuickPin(pin: String): Result<Boolean> = remoteResult {
        val response = api.verifyQuickPin(VerifyQuickPinRequestDto(pin = pin))
        response.unwrap { it.valid ?: false }
    }

    suspend fun changeQuickPin(currentPin: String, newPin: String): Result<Unit> = remoteResult {
        val req = ChangeQuickPinRequestDto(currentPin = currentPin, newPin = newPin)
        api.changeQuickPin(req).unwrap { Unit }
    }

    suspend fun updateBiometric(enabled: Boolean): Result<Unit> = remoteResult {
        api.updateBiometric(UpdateBiometricPreferenceRequestDto(enabled = enabled)).unwrap { Unit }
    }

    suspend fun updateSelectedStore(storeId: String): Result<Unit> = remoteResult {
        api.updateSelectedStore(UpdateSelectedStoreRequestDto(storeId = storeId)).unwrap { Unit }
    }

    suspend fun requestPasswordResetByEmail(email: String): Result<Unit> = remoteResult {
        api.passwordResetRequestByEmail(EmailPasswordResetRequestDto(email = email, channel = "EMAIL")).unwrap { Unit }
    }

    suspend fun requestQuickPinResetByEmail(email: String): Result<Unit> = remoteResult {
        api.quickPinResetRequestByEmail(EmailQuickPinResetRequestDto(email = email, channel = "EMAIL")).unwrap { Unit }
    }

    suspend fun verifyPasswordResetByEmail(email: String, code: String): Result<Unit> = remoteResult {
        api.passwordResetVerifyByEmail(EmailPasswordResetVerifyRequestDto(email = email, code = code)).unwrap { Unit }
    }

    suspend fun verifyQuickPinResetByEmail(email: String, code: String): Result<Unit> = remoteResult {
        api.quickPinResetVerifyByEmail(EmailQuickPinResetVerifyRequestDto(email = email, code = code)).unwrap { Unit }
    }

    suspend fun confirmPasswordResetByEmail(email: String, newPassword: String): Result<Unit> = remoteResult {
        api.passwordResetConfirmByEmail(EmailPasswordResetConfirmRequestDto(email = email, newPassword = newPassword)).unwrap { Unit }
    }

    suspend fun confirmQuickPinResetByEmail(email: String, newPin: String): Result<Unit> = remoteResult {
        api.quickPinResetConfirmByEmail(EmailQuickPinResetConfirmRequestDto(email = email, newPin = newPin)).unwrap { Unit }
    }
}

private fun List<String>?.toStaffPermissions(): StaffPermissions {
    val codes = this.orEmpty().map(String::lowercase).toSet()
    return StaffPermissions(
        viewAnalytics = "analytics.read" in codes,
        viewPrograms = "program.read" in codes || "program.write" in codes || "promotion.read" in codes || "promotion.write" in codes,
        managePrograms = "program.write" in codes || "promotion.write" in codes,
        processTransactions = "transaction.read" in codes || "transaction.write" in codes,
        manageCustomers = "customer.read" in codes || "customer.write" in codes,
        manageStaff = "staff.read" in codes || "staff.write" in codes,
        viewSettings = "settings.read" in codes || "billing.read" in codes,
    )
}
