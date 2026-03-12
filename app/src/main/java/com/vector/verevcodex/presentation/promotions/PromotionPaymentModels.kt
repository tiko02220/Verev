package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.promotions.Campaign
import java.time.temporal.ChronoUnit

internal data class NetworkPromotionBreakdown(
    val estimatedReach: Int,
    val totalLabel: String,
)

private const val NETWORK_PROMOTION_BASE_FEE = 50
private const val NETWORK_PROMOTION_DAILY_FEE = 10
private const val NETWORK_PROMOTION_REACH_BASE = 125_000
private const val NETWORK_PROMOTION_REACH_MULTIPLIER = 1_000

internal fun Campaign.toNetworkPromotionBreakdown(): NetworkPromotionBreakdown {
    val durationDays = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(0) + 1
    val total = NETWORK_PROMOTION_BASE_FEE + (durationDays * NETWORK_PROMOTION_DAILY_FEE)
    val estimatedReach = NETWORK_PROMOTION_REACH_BASE + (promotionValue * NETWORK_PROMOTION_REACH_MULTIPLIER).toInt()
    return NetworkPromotionBreakdown(
        estimatedReach = estimatedReach,
        totalLabel = "$" + String.format("%,.0f", total.toDouble()),
    )
}
