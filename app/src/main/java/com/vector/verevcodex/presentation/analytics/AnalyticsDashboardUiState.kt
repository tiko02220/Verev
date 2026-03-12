package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.BusinessAnalytics
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics

data class AnalyticsDashboardUiState(
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.WEEK,
    val businessAnalytics: BusinessAnalytics? = null,
    val staffAnalytics: List<StaffAnalytics> = emptyList(),
)
