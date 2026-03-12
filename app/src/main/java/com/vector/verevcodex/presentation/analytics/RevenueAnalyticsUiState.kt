package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown

data class RevenueAnalyticsUiState(
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val analytics: RevenueAnalyticsDrillDown? = null,
)
