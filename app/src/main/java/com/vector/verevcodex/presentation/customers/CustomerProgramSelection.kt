package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
internal fun List<RewardProgram>.eligibleCustomerPointsPrograms(): List<RewardProgram> =
    filter { it.active }

internal fun RewardProgram.manualTransactionPoints(amount: Double): Int {
    if (amount <= 0.0) return 0
    if (!configuration.earningEnabled) return 0
    val spendStep = configuration.pointsRule.spendStepAmount.coerceAtLeast(1)
    val stepCount = (amount / spendStep).toInt().coerceAtLeast(1)
    return stepCount * configuration.pointsRule.pointsAwardedPerStep.coerceAtLeast(1)
}

internal fun List<RewardProgram>.eligibleActivePrograms(): List<RewardProgram> =
    filter { it.active }

internal fun RewardProgram.programTypeLabelRes(): Int = when (type) {
    LoyaltyProgramType.POINTS -> R.string.merchant_points_rewards_title
    LoyaltyProgramType.DIGITAL_STAMP -> R.string.merchant_checkin_rewards_title
    LoyaltyProgramType.TIER -> R.string.merchant_tiered_loyalty_title
    LoyaltyProgramType.PURCHASE_FREQUENCY -> R.string.merchant_purchase_frequency_title
    LoyaltyProgramType.REFERRAL -> R.string.merchant_referral_rewards_title
}

internal fun RewardProgram.programEarnRateSummary(): String =
    rulesSummary
