package com.vector.verevcodex.presentation.customers

internal object CustomerCrmConstants {
    const val manualAdjustmentReason = "manual_adjustment"
    const val manualPurchaseSummary = "Manual customer transaction"
    const val manualRefundSummary = "Manual customer refund"
    private const val manualVisitDeltaPrefix = "visits="

    fun rewardRedemptionReason(rewardName: String): String = "reward_redeemed:$rewardName"

    fun couponRedemptionReason(couponName: String): String = "coupon_redeemed:$couponName"

    fun discountActionTitle(campaignName: String): String = campaignName

    fun discountActionDetails(description: String): String = description.ifBlank { "Discount applied for customer" }

    fun tierBenefitActionTitle(tierName: String): String = "$tierName tier support"

    fun tierBenefitActionDetails(tierName: String): String = "Tier benefit recorded for $tierName member"

    fun manualVisitActionTitle(programName: String, adding: Boolean): String =
        if (adding) "Manual progress added" else "Manual progress removed"

    fun manualVisitActionDetails(programName: String, delta: Int, reason: String): String =
        "$manualVisitDeltaPrefix$delta|program=$programName|reason=${reason.ifBlank { manualAdjustmentReason }}"

    fun parseManualVisitDelta(details: String): Int =
        details
            .substringAfter("$manualVisitDeltaPrefix", missingDelimiterValue = "")
            .substringBefore('|')
            .toIntOrNull()
            ?: 0
}
