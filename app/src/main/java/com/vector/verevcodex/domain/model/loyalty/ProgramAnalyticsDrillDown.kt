package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment

data class ProgramAnalyticsDrillDown(
    val storeId: String?,
    val totalPrograms: Int,
    val activePrograms: Int,
    val memberParticipationRate: Double,
    val redemptionEfficiency: Double,
    val typeBreakdown: List<AnalyticsSegment>,
    val rewardUsageBreakdown: List<AnalyticsSegment>,
    val scanActionBreakdown: List<AnalyticsSegment>,
    val topPrograms: List<ProgramPerformance>,
)
