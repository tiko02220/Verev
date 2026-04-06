package com.vector.verevcodex.data.remote.api

import com.vector.verevcodex.data.remote.api.auth.*
import retrofit2.Response
import retrofit2.http.*

interface VerevAuthApi {

    @POST("v1/auth/signup")
    suspend fun signup(@Body request: SignupRequestDto): Response<ApiEnvelope<AuthBundleResponseDto>>

    @POST("v1/auth/sessions/email")
    suspend fun loginByEmail(@Body request: EmailLoginRequestDto): Response<ApiEnvelope<LoginResponseDto>>

    @POST("v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): Response<ApiEnvelope<RefreshResponseDto>>

    @POST("v1/auth/logout")
    suspend fun logout(@Body request: LogoutRequestDto): Response<ApiEnvelope<LogoutResponseDto>>

    @GET("v1/auth/profile")
    suspend fun me(): Response<ApiEnvelope<MeResponseDto>>

    @PUT("v1/auth/profile")
    suspend fun updateMe(@Body request: UpdateMeRequestDto): Response<ApiEnvelope<MeResponseDto>>

    @POST("v1/auth/profile/password")
    suspend fun changePassword(@Body request: ChangePasswordRequestDto): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/auth/profile/password/activate")
    suspend fun activateInvitedPassword(@Body request: ActivateInvitedPasswordRequestDto): Response<ApiEnvelope<MeResponseDto>>

    @GET("v1/auth/security-preferences")
    suspend fun securityPreferences(): Response<ApiEnvelope<MerchantSecurityPreferencesViewDto>>

    @POST("v1/auth/security-preferences/setup")
    suspend fun setupSecurity(@Body request: SecuritySetupRequestDto): Response<ApiEnvelope<MerchantSecurityPreferencesViewDto>>

    @POST("v1/auth/security-preferences/quick-pin/verify")
    suspend fun verifyQuickPin(@Body request: VerifyQuickPinRequestDto): Response<ApiEnvelope<QuickPinVerificationResponseDto>>

    @PUT("v1/auth/security-preferences/quick-pin")
    suspend fun changeQuickPin(@Body request: ChangeQuickPinRequestDto): Response<ApiEnvelope<MerchantSecurityPreferencesViewDto>>

    @PUT("v1/auth/security-preferences/biometric")
    suspend fun updateBiometric(@Body request: UpdateBiometricPreferenceRequestDto): Response<ApiEnvelope<MerchantSecurityPreferencesViewDto>>

    @PUT("v1/auth/preferences/selected-store")
    suspend fun updateSelectedStore(@Body request: UpdateSelectedStoreRequestDto): Response<ApiEnvelope<MerchantSecurityPreferencesViewDto>>

    @POST("v1/auth/password-resets/email/request")
    suspend fun passwordResetRequestByEmail(@Body request: EmailPasswordResetRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/quick-pin-resets/email/request")
    suspend fun quickPinResetRequestByEmail(@Body request: EmailQuickPinResetRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/signup/email-verification/request")
    suspend fun signupEmailVerificationRequest(@Body request: EmailSignupVerificationRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/password-resets/email/verify")
    suspend fun passwordResetVerifyByEmail(@Body request: EmailPasswordResetVerifyRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/quick-pin-resets/email/verify")
    suspend fun quickPinResetVerifyByEmail(@Body request: EmailQuickPinResetVerifyRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/signup/email-verification/verify")
    suspend fun signupEmailVerificationVerify(@Body request: EmailSignupVerificationVerifyRequestDto): Response<ApiEnvelope<PasswordResetRequestResponseDto>>

    @POST("v1/auth/password-resets/email/confirm")
    suspend fun passwordResetConfirmByEmail(@Body request: EmailPasswordResetConfirmRequestDto): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/auth/quick-pin-resets/email/confirm")
    suspend fun quickPinResetConfirmByEmail(@Body request: EmailQuickPinResetConfirmRequestDto): Response<ApiEnvelope<Map<String, Boolean>>>
}
