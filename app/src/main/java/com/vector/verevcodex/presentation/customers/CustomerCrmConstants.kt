package com.vector.verevcodex.presentation.customers

internal object CustomerCrmConstants {
    const val manualAdjustmentReason = "manual_adjustment"

    fun rewardRedemptionReason(rewardName: String): String = "reward_redeemed:$rewardName"

    fun couponRedemptionReason(couponName: String): String = "coupon_redeemed:$couponName"

    fun discountActionTitle(campaignName: String): String = campaignName

    fun discountActionDetails(description: String): String = description.ifBlank { "Discount applied for customer" }

    fun tierBenefitActionTitle(tierName: String): String = "$tierName tier support"

    fun tierBenefitActionDetails(tierName: String): String = "Tier benefit recorded for $tierName member"
}
