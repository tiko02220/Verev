package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown

data class ProgramAnalyticsUiState(
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val analytics: ProgramAnalyticsDrillDown? = null,
)
