package com.vector.verevcodex.data.remote.api.loyalty

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VerevProgramsApi {
    @GET("v1/programs")
    suspend fun list(): Response<ApiEnvelope<List<LoyaltyProgramViewDto>>>

    @GET("v1/programs/active-scan-actions")
    suspend fun activeScanActions(@Query("storeId") storeId: String? = null): Response<ApiEnvelope<ActiveScanActionsResponseDto>>

    @POST("v1/programs")
    suspend fun create(
        @Body request: LoyaltyProgramRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<LoyaltyProgramViewDto>>

    @GET("v1/programs/{programId}")
    suspend fun get(@Path("programId") programId: String): Response<ApiEnvelope<LoyaltyProgramViewDto>>

    @PUT("v1/programs/{programId}")
    suspend fun update(
        @Path("programId") programId: String,
        @Body request: LoyaltyProgramRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<LoyaltyProgramViewDto>>

    @POST("v1/programs/{programId}/enable")
    suspend fun enable(
        @Path("programId") programId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<LoyaltyProgramViewDto>>

    @POST("v1/programs/{programId}/disable")
    suspend fun disable(
        @Path("programId") programId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<LoyaltyProgramViewDto>>

    @DELETE("v1/programs/{programId}")
    suspend fun delete(
        @Path("programId") programId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<Map<String, Boolean>>>
}

interface VerevRewardsApi {
    @GET("v1/rewards")
    suspend fun list(): Response<ApiEnvelope<List<RewardViewDto>>>

    @POST("v1/rewards")
    suspend fun create(
        @Body request: RewardRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RewardViewDto>>

    @GET("v1/rewards/{rewardId}")
    suspend fun get(@Path("rewardId") rewardId: String): Response<ApiEnvelope<RewardViewDto>>

    @PUT("v1/rewards/{rewardId}")
    suspend fun update(
        @Path("rewardId") rewardId: String,
        @Body request: RewardRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RewardViewDto>>

    @POST("v1/rewards/{rewardId}/enable")
    suspend fun enable(
        @Path("rewardId") rewardId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RewardViewDto>>

    @POST("v1/rewards/{rewardId}/disable")
    suspend fun disable(
        @Path("rewardId") rewardId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RewardViewDto>>

    @DELETE("v1/rewards/{rewardId}")
    suspend fun delete(
        @Path("rewardId") rewardId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/rewards/{rewardId}/inventory-adjustments")
    suspend fun adjustInventory(
        @Path("rewardId") rewardId: String,
        @Body request: RewardInventoryAdjustmentRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RewardViewDto>>
}

interface VerevCampaignsApi {
    @GET("v1/campaigns")
    suspend fun list(): Response<ApiEnvelope<List<CampaignViewDto>>>

    @POST("v1/campaigns")
    suspend fun create(
        @Body request: CampaignRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CampaignViewDto>>

    @GET("v1/campaigns/{campaignId}")
    suspend fun get(@Path("campaignId") campaignId: String): Response<ApiEnvelope<CampaignViewDto>>

    @PUT("v1/campaigns/{campaignId}")
    suspend fun update(
        @Path("campaignId") campaignId: String,
        @Body request: CampaignRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CampaignViewDto>>

    @POST("v1/campaigns/{campaignId}/enable")
    suspend fun enable(
        @Path("campaignId") campaignId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CampaignViewDto>>

    @POST("v1/campaigns/{campaignId}/disable")
    suspend fun disable(
        @Path("campaignId") campaignId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CampaignViewDto>>

    @DELETE("v1/campaigns/{campaignId}")
    suspend fun delete(
        @Path("campaignId") campaignId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<Map<String, Boolean>>>
}
