package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown

data class CustomerAnalyticsUiState(
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val isLoading: Boolean = true,
    val analytics: CustomerAnalyticsDrillDown? = null,
)
