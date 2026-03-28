package com.vector.verevcodex.data.remote.api.analytics

import com.google.gson.annotations.SerializedName

data class AnalyticsPointViewDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("value") val value: Double? = null,
)

data class AnalyticsSegmentViewDto(
    @SerializedName("label") val label: String? = null,
    @SerializedName("value") val value: Int? = null,
)

data class BusinessAnalyticsViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("scopeStoreId") val scopeStoreId: String? = null,
    @SerializedName("totalCustomers") val totalCustomers: Int? = null,
    @SerializedName("newCustomers") val newCustomers: Int? = null,
    @SerializedName("visitsToday") val visitsToday: Int? = null,
    @SerializedName("visitsInRange") val visitsInRange: Int? = null,
    @SerializedName("totalRevenue") val totalRevenue: Double? = null,
    @SerializedName("averagePurchaseValue") val averagePurchaseValue: Double? = null,
    @SerializedName("rewardRedemptionRate") val rewardRedemptionRate: Double? = null,
    @SerializedName("averagePromotionRoi") val averagePromotionRoi: Double? = null,
    @SerializedName("activePromotions") val activePromotions: Int? = null,
    @SerializedName("retentionRate") val retentionRate: Double? = null,
    @SerializedName("revenueGrowthRate") val revenueGrowthRate: Double? = null,
    @SerializedName("customerGrowthRate") val customerGrowthRate: Double? = null,
    @SerializedName("topCustomerName") val topCustomerName: String? = null,
    @SerializedName("topPromotionName") val topPromotionName: String? = null,
    @SerializedName("revenueTrend") val revenueTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("visitTrend") val visitTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("newCustomerTrend") val newCustomerTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("returningCustomerTrend") val returningCustomerTrend: List<AnalyticsPointViewDto>? = null,
)

data class TopCustomerInsightViewDto(
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("totalSpent") val totalSpent: Double? = null,
    @SerializedName("totalVisits") val totalVisits: Int? = null,
    @SerializedName("loyaltyTier") val loyaltyTier: String? = null,
)

data class CustomerAnalyticsDrillDownViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("hasTierAnalytics") val hasTierAnalytics: Boolean? = null,
    @SerializedName("totalCustomers") val totalCustomers: Int? = null,
    @SerializedName("newCustomers") val newCustomers: Int? = null,
    @SerializedName("returningCustomers") val returningCustomers: Int? = null,
    @SerializedName("retainedCustomers") val retainedCustomers: Int? = null,
    @SerializedName("inactiveCustomers") val inactiveCustomers: Int? = null,
    @SerializedName("highValueCustomers") val highValueCustomers: Int? = null,
    @SerializedName("averageLifetimeValue") val averageLifetimeValue: Double? = null,
    @SerializedName("topCustomers") val topCustomers: List<TopCustomerInsightViewDto>? = null,
    @SerializedName("tierBreakdown") val tierBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("segmentBreakdown") val segmentBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("newCustomerTrend") val newCustomerTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("returningCustomerTrend") val returningCustomerTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("retentionTrend") val retentionTrend: List<AnalyticsPointViewDto>? = null,
)

data class RevenueAnalyticsDrillDownViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("totalRevenue") val totalRevenue: Double? = null,
    @SerializedName("todayRevenue") val todayRevenue: Double? = null,
    @SerializedName("averageOrderValue") val averageOrderValue: Double? = null,
    @SerializedName("transactionCount") val transactionCount: Int? = null,
    @SerializedName("redeemedPointsValue") val redeemedPointsValue: Double? = null,
    @SerializedName("revenueGrowthRate") val revenueGrowthRate: Double? = null,
    @SerializedName("revenueTrend") val revenueTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("timeBucketTrend") val timeBucketTrend: List<AnalyticsPointViewDto>? = null,
    @SerializedName("sourceBreakdown") val sourceBreakdown: List<AnalyticsSegmentViewDto>? = null,
)

data class PromotionPerformanceViewDto(
    @SerializedName("promotionId") val promotionId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("paymentFlowEnabled") val paymentFlowEnabled: Boolean? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("estimatedUsageCount") val estimatedUsageCount: Int? = null,
    @SerializedName("revenueImpact") val revenueImpact: Double? = null,
    @SerializedName("roiScore") val roiScore: Double? = null,
)

data class PromotionAnalyticsDrillDownViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("totalPromotions") val totalPromotions: Int? = null,
    @SerializedName("activePromotions") val activePromotions: Int? = null,
    @SerializedName("scheduledPromotions") val scheduledPromotions: Int? = null,
    @SerializedName("expiredPromotions") val expiredPromotions: Int? = null,
    @SerializedName("paymentPromotions") val paymentPromotions: Int? = null,
    @SerializedName("averageRoiScore") val averageRoiScore: Double? = null,
    @SerializedName("typeBreakdown") val typeBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("statusBreakdown") val statusBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("topPromotions") val topPromotions: List<PromotionPerformanceViewDto>? = null,
)

data class ProgramPerformanceViewDto(
    @SerializedName("programId") val programId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("scanActionsEnabled") val scanActionsEnabled: Int? = null,
    @SerializedName("memberCount") val memberCount: Int? = null,
    @SerializedName("redemptionRate") val redemptionRate: Double? = null,
)

data class ProgramAnalyticsDrillDownViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("totalPrograms") val totalPrograms: Int? = null,
    @SerializedName("activePrograms") val activePrograms: Int? = null,
    @SerializedName("memberParticipationRate") val memberParticipationRate: Double? = null,
    @SerializedName("redemptionEfficiency") val redemptionEfficiency: Double? = null,
    @SerializedName("typeBreakdown") val typeBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("rewardUsageBreakdown") val rewardUsageBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("scanActionBreakdown") val scanActionBreakdown: List<AnalyticsSegmentViewDto>? = null,
    @SerializedName("topPrograms") val topPrograms: List<ProgramPerformanceViewDto>? = null,
)

data class StaffAnalyticsViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("staffId") val staffId: String? = null,
    @SerializedName("staffName") val staffName: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("transactionsProcessed") val transactionsProcessed: Int? = null,
    @SerializedName("revenueHandled") val revenueHandled: Double? = null,
    @SerializedName("customersServed") val customersServed: Int? = null,
    @SerializedName("rewardsRedeemed") val rewardsRedeemed: Int? = null,
    @SerializedName("averageTransactionValue") val averageTransactionValue: Double? = null,
)

data class DashboardProgramSummaryViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("scanActions") val scanActions: Set<String>? = null,
)

data class DashboardCampaignSummaryViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("promotionType") val promotionType: String? = null,
    @SerializedName("active") val active: Boolean? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
)

data class DashboardTransactionSummaryViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("pointsEarned") val pointsEarned: Int? = null,
    @SerializedName("pointsRedeemed") val pointsRedeemed: Int? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("occurredAt") val occurredAt: String? = null,
)

data class DashboardHealthCheckViewDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("affectedCount") val affectedCount: Int? = null,
)

data class DashboardHealthSnapshotViewDto(
    @SerializedName("healthy") val healthy: Boolean? = null,
    @SerializedName("checks") val checks: List<DashboardHealthCheckViewDto>? = null,
)

data class MerchantDashboardSnapshotViewDto(
    @SerializedName("scopeStoreId") val scopeStoreId: String? = null,
    @SerializedName("stores") val stores: List<com.vector.verevcodex.data.remote.api.store.StoreViewDto>? = null,
    @SerializedName("analytics") val analytics: BusinessAnalyticsViewDto? = null,
    @SerializedName("health") val health: DashboardHealthSnapshotViewDto? = null,
    @SerializedName("activePrograms") val activePrograms: List<DashboardProgramSummaryViewDto>? = null,
    @SerializedName("activeCampaigns") val activeCampaigns: List<DashboardCampaignSummaryViewDto>? = null,
    @SerializedName("topStaff") val topStaff: List<StaffAnalyticsViewDto>? = null,
    @SerializedName("recentTransactions") val recentTransactions: List<DashboardTransactionSummaryViewDto>? = null,
)
