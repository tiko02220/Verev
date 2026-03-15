package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.promotions.PromotionBoostLevel
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import com.vector.verevcodex.presentation.merchant.common.formatWholeCurrency
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class NetworkPromotionBreakdown(
    val durationDays: Int,
    val baseFee: Double,
    val durationFee: Double,
    val boostFee: Double,
    val estimatedReach: Int,
    val estimatedNewCustomers: Int,
    val totalCost: Double,
    val totalLabel: String,
)

private const val NETWORK_PROMOTION_BASE_FEE = 50.0
private const val NETWORK_PROMOTION_DAILY_FEE = 15.0
private const val NETWORK_PROMOTION_CONVERSION_RATE = 0.05

internal fun Campaign.toNetworkPromotionBreakdown(): NetworkPromotionBreakdown =
    buildNetworkPromotionBreakdown(
        startDate = startDate,
        endDate = endDate,
        visibility = visibility,
        boostLevel = boostLevel ?: PromotionBoostLevel.STANDARD,
    )

internal fun PromotionEditorState.toPreviewCampaign(storeId: String): Campaign {
    val start = runCatching { LocalDate.parse(startDate) }.getOrDefault(LocalDate.now())
    val end = runCatching { LocalDate.parse(endDate) }.getOrDefault(start.plusDays(4))
    return Campaign(
        id = promotionId ?: "draft-promotion",
        storeId = storeId,
        name = name.trim(),
        description = description.trim(),
        imageUri = imageUri.ifBlank { null },
        startDate = start,
        endDate = end,
        promotionType = promotionType,
        promotionValue = promotionType.defaultValueForInput(promotionValue),
        minimumPurchaseAmount = minimumPurchaseAmount.toDoubleOrNull() ?: 0.0,
        usageLimit = usageLimit.toIntOrNull() ?: 0,
        promoCode = promoCode.ifBlank { null },
        visibility = visibility,
        boostLevel = if (visibility == PromotionVisibility.NETWORK_WIDE) boostLevel else null,
        paymentFlowEnabled = visibility == PromotionVisibility.NETWORK_WIDE,
        active = active,
        target = CampaignTarget(
            id = "draft-target",
            campaignId = promotionId ?: "draft-promotion",
            segment = targetSegment,
            description = targetDescription.ifBlank { targetSegment.name },
        ),
    )
}

internal fun buildNetworkPromotionBreakdown(
    startDate: LocalDate,
    endDate: LocalDate,
    visibility: PromotionVisibility,
    boostLevel: PromotionBoostLevel,
): NetworkPromotionBreakdown {
    val durationDays = ChronoUnit.DAYS.between(startDate, endDate).toInt().coerceAtLeast(0) + 1
    val normalizedBoost = if (visibility == PromotionVisibility.NETWORK_WIDE) boostLevel else PromotionBoostLevel.STANDARD
    val baseFee = if (visibility == PromotionVisibility.NETWORK_WIDE) NETWORK_PROMOTION_BASE_FEE else 0.0
    val durationFee = if (visibility == PromotionVisibility.NETWORK_WIDE) durationDays * NETWORK_PROMOTION_DAILY_FEE else 0.0
    val boostFee = if (visibility == PromotionVisibility.NETWORK_WIDE) normalizedBoost.additionalCost else 0.0
    val estimatedReach = if (visibility == PromotionVisibility.NETWORK_WIDE) normalizedBoost.estimatedReach else 0
    val estimatedNewCustomers = (estimatedReach * NETWORK_PROMOTION_CONVERSION_RATE).toInt()
    val totalCost = baseFee + durationFee + boostFee

    return NetworkPromotionBreakdown(
        durationDays = durationDays,
        baseFee = baseFee,
        durationFee = durationFee,
        boostFee = boostFee,
        estimatedReach = estimatedReach,
        estimatedNewCustomers = estimatedNewCustomers,
        totalCost = totalCost,
        totalLabel = formatWholeCurrency(totalCost),
    )
}

internal val PromotionBoostLevel.additionalCost: Double
    get() = when (this) {
        PromotionBoostLevel.STANDARD -> 0.0
        PromotionBoostLevel.FEATURED -> 100.0
        PromotionBoostLevel.PREMIUM -> 250.0
    }

internal val PromotionBoostLevel.estimatedReach: Int
    get() = when (this) {
        PromotionBoostLevel.STANDARD -> 37_500
        PromotionBoostLevel.FEATURED -> 75_000
        PromotionBoostLevel.PREMIUM -> 125_000
    }
