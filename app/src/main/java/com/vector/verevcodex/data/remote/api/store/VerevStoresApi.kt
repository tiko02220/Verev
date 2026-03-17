package com.vector.verevcodex.data.remote.api.store

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VerevStoresApi {

    @GET("v1/stores")
    suspend fun list(): Response<ApiEnvelope<List<StoreViewDto>>>

    @GET("v1/stores/{storeId}")
    suspend fun get(@Path("storeId") storeId: String): Response<ApiEnvelope<StoreViewDto>>

    @POST("v1/stores")
    suspend fun create(
        @Body request: CreateStoreRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<StoreViewDto>>

    @PUT("v1/stores/{storeId}")
    suspend fun update(
        @Path("storeId") storeId: String,
        @Body request: UpdateStoreRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<StoreViewDto>>

    @POST("v1/stores/{storeId}/activate")
    suspend fun activate(
        @Path("storeId") storeId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<StoreViewDto>>

    @POST("v1/stores/{storeId}/deactivate")
    suspend fun deactivate(
        @Path("storeId") storeId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<StoreViewDto>>

    @GET("v1/stores/{storeId}/branding")
    suspend fun branding(@Path("storeId") storeId: String): Response<ApiEnvelope<BrandingSettingsViewDto>>

    @PUT("v1/stores/{storeId}/branding")
    suspend fun updateBranding(
        @Path("storeId") storeId: String,
        @Body request: UpdateBrandingRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<BrandingSettingsViewDto>>

    @GET("v1/stores/{storeId}/configuration")
    suspend fun configuration(@Path("storeId") storeId: String): Response<ApiEnvelope<BranchConfigurationViewDto>>

    @PUT("v1/stores/{storeId}/configuration")
    suspend fun updateConfiguration(
        @Path("storeId") storeId: String,
        @Body request: UpdateBranchConfigurationRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<BranchConfigurationViewDto>>
}
