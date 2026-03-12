package com.vector.verevcodex.domain.model.analytics

import com.vector.verevcodex.domain.model.common.Identifiable

data class BusinessAnalytics(
    override val id: String,
    val scopeStoreId: String?,
    val totalCustomers: Int,
    val newCustomers: Int,
    val visitsToday: Int,
    val totalRevenue: Double,
    val averagePurchaseValue: Double,
    val rewardRedemptionRate: Double,
    val retentionRate: Double,
    val revenueGrowthRate: Double,
    val customerGrowthRate: Double,
    val topCustomerName: String,
    val topPromotionName: String,
    val revenueTrend: List<AnalyticsPoint>,
    val visitTrend: List<AnalyticsPoint>,
) : Identifiable
