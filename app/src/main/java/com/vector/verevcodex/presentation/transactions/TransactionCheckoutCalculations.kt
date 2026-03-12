package com.vector.verevcodex.presentation.transactions

import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionType
import kotlin.math.min
import kotlin.math.roundToInt

internal data class ParsedCheckoutLineItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
)

internal fun computeCheckoutTotals(
    items: List<ParsedCheckoutLineItem>,
    availablePoints: Int,
    redeemPointsInput: String,
    applyPointRedemption: Boolean,
    selectedPromotion: Campaign? = null,
): CheckoutTotals {
    val subtotal = items.sumOf { it.quantity * it.unitPrice }
    val promotionDiscount = when (selectedPromotion?.promotionType) {
        PromotionType.PERCENT_DISCOUNT -> (subtotal * (selectedPromotion.promotionValue / 100.0)).coerceAtMost(subtotal)
        PromotionType.FIXED_DISCOUNT -> selectedPromotion.promotionValue.coerceAtMost(subtotal)
        else -> 0.0
    }
    val subtotalAfterPromotion = (subtotal - promotionDiscount).coerceAtLeast(0.0)
    val maxRedeemablePoints = min(availablePoints, subtotalAfterPromotion.roundToInt().coerceAtLeast(0))
    val requestedPoints = redeemPointsInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val redeemedPoints = if (applyPointRedemption) min(requestedPoints, maxRedeemablePoints) else 0
    val finalAmount = (subtotalAfterPromotion - redeemedPoints).coerceAtLeast(0.0)
    val baseEarnedPoints = if (finalAmount <= 0.0) 0 else (finalAmount / 100.0).roundToInt().coerceAtLeast(1)
    val promotionBonusPoints = when (selectedPromotion?.promotionType) {
        PromotionType.POINTS_MULTIPLIER -> ((baseEarnedPoints * selectedPromotion.promotionValue) - baseEarnedPoints).roundToInt().coerceAtLeast(0)
        PromotionType.BONUS_POINTS -> selectedPromotion.promotionValue.roundToInt().coerceAtLeast(0)
        else -> 0
    }
    val pointsEarned = baseEarnedPoints + promotionBonusPoints
    return CheckoutTotals(
        subtotal = subtotal,
        promotionDiscount = promotionDiscount,
        promotionBonusPoints = promotionBonusPoints,
        redeemablePoints = maxRedeemablePoints,
        redeemedPoints = redeemedPoints,
        finalAmount = finalAmount,
        pointsEarned = pointsEarned,
    )
}

internal fun buildTransactionSummary(
    note: String,
    items: List<ParsedCheckoutLineItem>,
    selectedPromotion: Campaign? = null,
): String {
    val trimmedNote = note.trim()
    val lineSummary = items.joinToString(separator = " + ") { parsed ->
        if (parsed.quantity > 1) {
            "${parsed.name} x${parsed.quantity}"
        } else {
            parsed.name
        }
    }.ifBlank { "Checkout sale" }
    val promotionSummary = selectedPromotion?.name?.let { promotionName -> "Promotion: $promotionName" }
    return listOf(trimmedNote.ifBlank { lineSummary }, promotionSummary)
        .filterNotNull()
        .filter { it.isNotBlank() }
        .joinToString(" • ")
}
