package com.vector.verevcodex.presentation.analytics

import com.vector.verevcodex.domain.model.analytics.StaffAnalytics

data class StaffAnalyticsUiState(
    val staffAnalytics: List<StaffAnalytics> = emptyList(),
)
