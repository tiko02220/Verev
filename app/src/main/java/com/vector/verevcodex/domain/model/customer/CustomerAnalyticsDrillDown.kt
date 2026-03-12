package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.analytics.AnalyticsPoint
import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment

data class CustomerAnalyticsDrillDown(
    val storeId: String?,
    val totalCustomers: Int,
    val newCustomers: Int,
    val returningCustomers: Int,
    val retainedCustomers: Int,
    val inactiveCustomers: Int,
    val highValueCustomers: Int,
    val averageLifetimeValue: Double,
    val topCustomers: List<TopCustomerAnalytics>,
    val tierBreakdown: List<AnalyticsSegment>,
    val segmentBreakdown: List<AnalyticsSegment>,
    val activityTrend: List<AnalyticsPoint>,
    val retentionTrend: List<AnalyticsPoint>,
)
