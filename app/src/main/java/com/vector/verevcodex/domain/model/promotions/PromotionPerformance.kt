package com.vector.verevcodex.domain.model.promotions

data class PromotionPerformance(
    val promotionId: String,
    val name: String,
    val type: PromotionType,
    val paymentFlowEnabled: Boolean,
    val active: Boolean,
    val estimatedUsageCount: Int,
    val revenueImpact: Double,
    val roiScore: Double,
)
