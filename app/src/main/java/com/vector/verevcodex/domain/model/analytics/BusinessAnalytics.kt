package com.vector.verevcodex.domain.model.analytics

import com.vector.verevcodex.domain.model.common.Identifiable

data class BusinessAnalytics(
    override val id: String,
    val scopeStoreId: String?,
    val totalCustomers: Int,
    val newCustomers: Int,
    val visitsToday: Int,
    val visitsInRange: Int,
    val totalRevenue: Double,
    val averagePurchaseValue: Double,
    val rewardRedemptionRate: Double,
    val averagePromotionRoi: Double,
    val activePromotions: Int,
    val retentionRate: Double,
    val revenueGrowthRate: Double,
    val customerGrowthRate: Double,
    val topCustomerName: String,
    val topPromotionName: String,
    val revenueTrend: List<AnalyticsPoint>,
    val visitTrend: List<AnalyticsPoint>,
    val newCustomerTrend: List<AnalyticsPoint>,
    val returningCustomerTrend: List<AnalyticsPoint>,
) : Identifiable {
    companion object {
        fun empty(scopeStoreId: String?) = BusinessAnalytics(
            id = "empty",
            scopeStoreId = scopeStoreId,
            totalCustomers = 0,
            newCustomers = 0,
            visitsToday = 0,
            visitsInRange = 0,
            totalRevenue = 0.0,
            averagePurchaseValue = 0.0,
            rewardRedemptionRate = 0.0,
            averagePromotionRoi = 0.0,
            activePromotions = 0,
            retentionRate = 0.0,
            revenueGrowthRate = 0.0,
            customerGrowthRate = 0.0,
            topCustomerName = "",
            topPromotionName = "",
            revenueTrend = emptyList(),
            visitTrend = emptyList(),
            newCustomerTrend = emptyList(),
            returningCustomerTrend = emptyList(),
        )
    }
}
