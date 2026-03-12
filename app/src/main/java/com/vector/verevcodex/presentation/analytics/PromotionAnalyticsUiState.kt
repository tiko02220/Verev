package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown

data class PromotionAnalyticsUiState(
    val selectedRange: AnalyticsTimeRange = AnalyticsTimeRange.MONTH,
    val isLoading: Boolean = true,
    val analytics: PromotionAnalyticsDrillDown? = null,
)
