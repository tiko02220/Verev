package com.vector.verevcodex.data.remote.api.auth

import com.google.gson.annotations.SerializedName

// --- Signup (request) ---
data class SignupRequestDto(
    @SerializedName("organization") val organization: OrganizationSignupPayloadDto,
    @SerializedName("owner") val owner: OwnerSignupPayloadDto,
    @SerializedName("store") val store: StoreSignupPayloadDto,
)

data class OrganizationSignupPayloadDto(
    @SerializedName("legalName") val legalName: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("industry") val industry: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("defaultCurrencyCode") val defaultCurrencyCode: String,
    @SerializedName("defaultTimezone") val defaultTimezone: String,
)

data class OwnerSignupPayloadDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("password") val password: String,
)

data class StoreSignupPayloadDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("contactInfo") val contactInfo: String,
    @SerializedName("category") val category: String,
    @SerializedName("workingHours") val workingHours: String,
)

// --- Login ---
data class EmailLoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class RefreshRequestDto(
    @SerializedName("refreshToken") val refreshToken: String,
)

data class LogoutRequestDto(
    @SerializedName("refreshToken") val refreshToken: String,
)

// --- Password / PIN reset (email-based) ---
data class EmailPasswordResetRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("channel") val channel: String,
)

data class EmailQuickPinResetRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("channel") val channel: String,
)

data class EmailPasswordResetVerifyRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
)

data class EmailQuickPinResetVerifyRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String,
)

data class EmailPasswordResetConfirmRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("newPassword") val newPassword: String,
)

data class EmailQuickPinResetConfirmRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("newPin") val newPin: String,
)

// --- Me / profile ---
data class UpdateMeRequestDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("profilePhotoUri") val profilePhotoUri: String?,
)

data class ChangePasswordRequestDto(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String,
)

data class ActivateInvitedPasswordRequestDto(
    @SerializedName("newPassword") val newPassword: String,
)

data class SecuritySetupRequestDto(
    @SerializedName("pin") val pin: String,
    @SerializedName("biometricEnabled") val biometricEnabled: Boolean,
    @SerializedName("selectedStoreId") val selectedStoreId: String?,
)

data class VerifyQuickPinRequestDto(
    @SerializedName("pin") val pin: String,
)

data class ChangeQuickPinRequestDto(
    @SerializedName("currentPin") val currentPin: String,
    @SerializedName("newPin") val newPin: String,
)

data class UpdateBiometricPreferenceRequestDto(
    @SerializedName("enabled") val enabled: Boolean,
)

data class UpdateSelectedStoreRequestDto(
    @SerializedName("storeId") val storeId: String,
)

// --- Responses ---
data class LoginResponseDto(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("user") val user: UserViewDto? = null,
    @SerializedName("organization") val organization: OrganizationViewDto? = null,
    @SerializedName("tenantScope") val tenantScope: TenantScopeViewDto? = null,
)

data class AuthBundleResponseDto(
    @SerializedName("organization") val organization: OrganizationViewDto? = null,
    @SerializedName("user") val user: UserViewDto? = null,
    @SerializedName("defaultStore") val defaultStore: StoreViewDto? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("tenantScope") val tenantScope: TenantScopeViewDto? = null,
)

data class RefreshResponseDto(
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("refreshToken") val refreshToken: String? = null,
)

data class MeResponseDto(
    @SerializedName("user") val user: UserViewDto? = null,
    @SerializedName("organization") val organization: OrganizationViewDto? = null,
    @SerializedName("tenantScope") val tenantScope: TenantScopeViewDto? = null,
)

data class MerchantSecurityPreferencesViewDto(
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("selectedStoreId") val selectedStoreId: String? = null,
    @SerializedName("quickPinConfigured") val quickPinConfigured: Boolean? = null,
    @SerializedName("biometricEnabled") val biometricEnabled: Boolean? = null,
    @SerializedName("activationRequired") val activationRequired: Boolean? = null,
)

data class QuickPinVerificationResponseDto(
    @SerializedName("valid") val valid: Boolean? = null,
)

data class PasswordResetRequestResponseDto(
    @SerializedName("resetRequestId") val resetRequestId: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
)

data class LogoutResponseDto(
    @SerializedName("revoked") val revoked: Boolean? = null,
)

data class UserViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("profilePhotoUri") val profilePhotoUri: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null,
)

data class OrganizationViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("legalName") val legalName: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("industry") val industry: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("accessState") val accessState: String? = null,
    @SerializedName("defaultCurrencyCode") val defaultCurrencyCode: String? = null,
    @SerializedName("defaultTimezone") val defaultTimezone: String? = null,
)

data class StoreViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("contactInfo") val contactInfo: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("workingHours") val workingHours: String? = null,
    @SerializedName("logoUrl") val logoUrl: String? = null,
    @SerializedName("primaryColor") val primaryColor: String? = null,
    @SerializedName("secondaryColor") val secondaryColor: String? = null,
    @SerializedName("active") val active: Boolean? = null,
)

data class TenantScopeViewDto(
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("accessibleStoreIds") val accessibleStoreIds: List<String>? = null,
    @SerializedName("permissions") val permissions: List<String>? = null,
)
