package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics

data class StaffAnalyticsUiState(
    val isLoading: Boolean = true,
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val staffAnalytics: List<StaffAnalytics> = emptyList(),
)
