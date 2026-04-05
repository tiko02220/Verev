package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType

enum class ProgramRewardOutcomeType {
    POINTS,
    FREE_PRODUCT,
    DISCOUNT_COUPON,
    GIFT_ITEM,
    SPECIAL_PROMOTION,
    PROGRAM_POINTS,
    PROGRAM_DIGITAL_STAMP,
    PROGRAM_TIER,
    PROGRAM_COUPON,
    PROGRAM_PURCHASE_FREQUENCY,
    PROGRAM_REFERRAL,
    ;

    companion object {
        fun fromApi(value: String?): ProgramRewardOutcomeType =
            when (value?.trim()?.uppercase()?.replace('-', '_')?.replace(' ', '_')) {
                "FREE_PRODUCT" -> FREE_PRODUCT
                "DISCOUNT_COUPON" -> DISCOUNT_COUPON
                "GIFT_ITEM" -> GIFT_ITEM
                "SPECIAL_PROMOTION" -> SPECIAL_PROMOTION
                "PROGRAM_POINTS" -> PROGRAM_POINTS
                "PROGRAM_DIGITAL_STAMP", "PROGRAM_STAMP" -> PROGRAM_DIGITAL_STAMP
                "PROGRAM_TIER" -> PROGRAM_TIER
                "PROGRAM_COUPON", "COUPON_BENEFIT", "COUPON" -> PROGRAM_COUPON
                "PROGRAM_PURCHASE_FREQUENCY" -> PROGRAM_PURCHASE_FREQUENCY
                "PROGRAM_REFERRAL" -> PROGRAM_REFERRAL
                "REWARD_ITEM", "REWARD" -> FREE_PRODUCT
                "PROGRAM_BENEFIT", "PROGRAM", "PROGRAM_UNLOCK", "CASHBACK_BENEFIT", "CASHBACK" -> PROGRAM_POINTS
                else -> POINTS
            }
    }
}

data class ProgramRewardOutcome(
    val type: ProgramRewardOutcomeType = ProgramRewardOutcomeType.POINTS,
    val label: String = "",
    val pointsAmount: Int = 0,
    val rewardId: String? = null,
    val rewardName: String = "",
    val rewardType: RewardType? = null,
    val programId: String? = null,
    val programName: String = "",
)

fun ProgramRewardOutcome.displayValue(): String = when (type) {
    ProgramRewardOutcomeType.POINTS -> "${pointsAmount.coerceAtLeast(0)} pts"
    else -> when {
        rewardName.isNotBlank() -> rewardName
        programName.isNotBlank() -> programName
        label.isNotBlank() -> label
        else -> ""
    }
}

fun ProgramRewardOutcomeType.usesRewardItem(): Boolean = when (this) {
    ProgramRewardOutcomeType.FREE_PRODUCT,
    ProgramRewardOutcomeType.DISCOUNT_COUPON,
    ProgramRewardOutcomeType.GIFT_ITEM,
    ProgramRewardOutcomeType.SPECIAL_PROMOTION,
    -> true
    else -> false
}

fun ProgramRewardOutcomeType.usesProgramBenefit(): Boolean = when (this) {
    ProgramRewardOutcomeType.PROGRAM_POINTS,
    ProgramRewardOutcomeType.PROGRAM_DIGITAL_STAMP,
    ProgramRewardOutcomeType.PROGRAM_TIER,
    ProgramRewardOutcomeType.PROGRAM_COUPON,
    ProgramRewardOutcomeType.PROGRAM_PURCHASE_FREQUENCY,
    ProgramRewardOutcomeType.PROGRAM_REFERRAL,
    -> true
    else -> false
}

fun ProgramRewardOutcomeType.impliedRewardType(): RewardType? = when (this) {
    ProgramRewardOutcomeType.FREE_PRODUCT -> RewardType.FREE_PRODUCT
    ProgramRewardOutcomeType.DISCOUNT_COUPON -> RewardType.DISCOUNT_COUPON
    ProgramRewardOutcomeType.GIFT_ITEM -> RewardType.GIFT_ITEM
    ProgramRewardOutcomeType.SPECIAL_PROMOTION -> RewardType.SPECIAL_PROMOTION
    else -> null
}

fun ProgramRewardOutcomeType.impliedProgramType(): LoyaltyProgramType? = when (this) {
    ProgramRewardOutcomeType.PROGRAM_POINTS -> LoyaltyProgramType.POINTS
    ProgramRewardOutcomeType.PROGRAM_DIGITAL_STAMP -> LoyaltyProgramType.DIGITAL_STAMP
    ProgramRewardOutcomeType.PROGRAM_TIER -> LoyaltyProgramType.TIER
    ProgramRewardOutcomeType.PROGRAM_COUPON -> LoyaltyProgramType.COUPON
    ProgramRewardOutcomeType.PROGRAM_PURCHASE_FREQUENCY -> LoyaltyProgramType.PURCHASE_FREQUENCY
    ProgramRewardOutcomeType.PROGRAM_REFERRAL -> LoyaltyProgramType.REFERRAL
    else -> null
}
