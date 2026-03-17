package com.vector.verevcodex.data.remote.api.analytics

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VerevAnalyticsApi {

    @GET("v1/analytics/dashboard-snapshot")
    suspend fun dashboardSnapshot(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "WEEK",
    ): Response<ApiEnvelope<MerchantDashboardSnapshotViewDto>>

    @GET("v1/analytics/business")
    suspend fun business(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "WEEK",
    ): Response<ApiEnvelope<BusinessAnalyticsViewDto>>

    @GET("v1/analytics/customers")
    suspend fun customers(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "MONTH",
    ): Response<ApiEnvelope<CustomerAnalyticsDrillDownViewDto>>

    @GET("v1/analytics/revenue")
    suspend fun revenue(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "MONTH",
    ): Response<ApiEnvelope<RevenueAnalyticsDrillDownViewDto>>

    @GET("v1/analytics/promotions")
    suspend fun promotions(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "MONTH",
    ): Response<ApiEnvelope<PromotionAnalyticsDrillDownViewDto>>

    @GET("v1/analytics/programs")
    suspend fun programs(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "MONTH",
    ): Response<ApiEnvelope<ProgramAnalyticsDrillDownViewDto>>

    @GET("v1/analytics/staff")
    suspend fun staff(
        @Query("storeId") storeId: String? = null,
        @Query("range") range: String = "MONTH",
    ): Response<ApiEnvelope<List<StaffAnalyticsViewDto>>>
}
