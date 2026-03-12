package com.vector.verevcodex.domain.model.analytics

data class RevenueAnalyticsDrillDown(
    val storeId: String?,
    val totalRevenue: Double,
    val todayRevenue: Double,
    val averageOrderValue: Double,
    val transactionCount: Int,
    val redeemedPointsValue: Double,
    val revenueGrowthRate: Double,
    val revenueTrend: List<AnalyticsPoint>,
    val timeBucketTrend: List<AnalyticsPoint>,
    val sourceBreakdown: List<AnalyticsSegment>,
)
