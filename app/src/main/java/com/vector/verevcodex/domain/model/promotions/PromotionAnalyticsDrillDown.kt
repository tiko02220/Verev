package com.vector.verevcodex.domain.model.promotions

import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment

data class PromotionAnalyticsDrillDown(
    val storeId: String?,
    val totalPromotions: Int,
    val activePromotions: Int,
    val scheduledPromotions: Int,
    val expiredPromotions: Int,
    val paymentPromotions: Int,
    val averageRoiScore: Double,
    val typeBreakdown: List<AnalyticsSegment>,
    val statusBreakdown: List<AnalyticsSegment>,
    val topPromotions: List<PromotionPerformance>,
)
