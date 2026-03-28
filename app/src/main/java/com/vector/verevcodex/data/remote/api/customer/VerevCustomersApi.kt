package com.vector.verevcodex.data.remote.api.customer

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VerevCustomersApi {

    @GET("v1/customers")
    suspend fun list(
        @Query("storeId") storeId: String? = null,
        @Query("search") search: String? = null,
        @Query("tier") tier: String? = null,
        @Query("inactive") inactive: Boolean? = null,
        @Query("limit") limit: Int = 20,
    ): Response<ApiEnvelope<List<CustomerDetailResponseDto>>>

    @GET("v1/customers/{customerId}")
    suspend fun get(@Path("customerId") customerId: String): Response<ApiEnvelope<CustomerDetailResponseDto>>

    @POST("v1/customers")
    suspend fun create(
        @Body request: CreateCustomerRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerDetailResponseDto>>

    @POST("v1/customers/quick-register")
    suspend fun quickRegister(
        @Body request: QuickRegisterCustomerRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerDetailResponseDto>>

    @PUT("v1/customers/{customerId}")
    suspend fun update(
        @Path("customerId") customerId: String,
        @Body request: UpdateCustomerRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerDetailResponseDto>>

    @GET("v1/customers/{customerId}/memberships")
    suspend fun memberships(@Path("customerId") customerId: String): Response<ApiEnvelope<List<CustomerMembershipViewDto>>>

    @PUT("v1/customers/{customerId}/memberships/{storeId}")
    suspend fun upsertMembership(
        @Path("customerId") customerId: String,
        @Path("storeId") storeId: String,
        @Body request: UpdateMembershipRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerMembershipViewDto>>

    @GET("v1/customers/{customerId}/credentials")
    suspend fun credentials(@Path("customerId") customerId: String): Response<ApiEnvelope<List<CustomerCredentialViewDto>>>

    @POST("v1/customers/{customerId}/credentials")
    suspend fun createCredential(
        @Path("customerId") customerId: String,
        @Body request: CreateCustomerCredentialRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerCredentialViewDto>>

    @PATCH("v1/customers/{customerId}/credentials/{credentialId}")
    suspend fun patchCredential(
        @Path("customerId") customerId: String,
        @Path("credentialId") credentialId: String,
        @Body request: PatchCustomerCredentialRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerCredentialViewDto>>

    @GET("v1/customers/{customerId}/bonus-actions")
    suspend fun bonusActions(@Path("customerId") customerId: String): Response<ApiEnvelope<List<CustomerBonusActionViewDto>>>

    @POST("v1/customers/{customerId}/bonus-actions")
    suspend fun createBonusAction(
        @Path("customerId") customerId: String,
        @Body request: CreateCustomerBonusActionRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerBonusActionViewDto>>

    @GET("v1/customers/{customerId}/points-ledger")
    suspend fun pointsLedger(
        @Path("customerId") customerId: String,
        @Query("limit") limit: Int = 100,
    ): Response<ApiEnvelope<List<PointsLedgerEntryDto>>>

    @POST("v1/customers/{customerId}/points-adjustments")
    suspend fun adjustPoints(
        @Path("customerId") customerId: String,
        @Body request: ManualPointsAdjustmentRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<ManualPointsAdjustmentResponseDto>>

    @POST("v1/customers/{customerId}/visit-adjustments")
    suspend fun adjustVisits(
        @Path("customerId") customerId: String,
        @Body request: ManualVisitAdjustmentRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<ManualVisitAdjustmentResponseDto>>

    @GET("v1/customers/by-loyalty-id/{loyaltyId}")
    suspend fun byLoyaltyId(
        @Path("loyaltyId") loyaltyId: String,
        @Query("storeId") storeId: String? = null,
    ): Response<ApiEnvelope<CustomerDetailResponseDto>>

    @GET("v1/customers/merge-preview")
    suspend fun mergePreview(
        @Query("sourceCustomerId") sourceCustomerId: String,
        @Query("targetCustomerId") targetCustomerId: String,
    ): Response<ApiEnvelope<CustomerMergePreviewDto>>

    @POST("v1/customers/merge")
    suspend fun merge(
        @Body request: MergeCustomersRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerMergeResultDto>>

    @GET("v1/customers/split-preview")
    suspend fun splitPreview(
        @Query("sourceCustomerId") sourceCustomerId: String,
        @Query("organizationId") organizationId: String,
    ): Response<ApiEnvelope<CustomerSplitPreviewDto>>

    @POST("v1/customers/split")
    suspend fun split(
        @Body request: SplitCustomerRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CustomerSplitResultDto>>
}
