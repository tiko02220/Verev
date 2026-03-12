package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import kotlin.math.roundToInt

internal fun List<RewardProgram>.resolveProgramFor(action: RewardProgramScanAction): RewardProgram? =
    asSequence()
        .filter { program ->
            program.active &&
                action in program.configuration.scanActions &&
                action.isEnabledIn(program.configuration)
        }
        .sortedBy { program -> program.priorityFor(action) }
        .firstOrNull()

internal fun List<RewardProgram>.calculateEarnedPoints(amount: Double): Int {
    if (amount <= 0.0) return 0
    val rule = resolveProgramFor(RewardProgramScanAction.EARN_POINTS)?.configuration?.pointsRule ?: PointsProgramRule()
    val spendStep = rule.spendStepAmount.coerceAtLeast(1)
    val stepCount = (amount / spendStep).toInt().coerceAtLeast(1)
    return stepCount * rule.pointsAwardedPerStep.coerceAtLeast(1)
}

internal fun List<RewardProgram>.minimumRedeemPoints(): Int {
    val program = resolveProgramFor(RewardProgramScanAction.REDEEM_REWARDS) ?: return PointsProgramRule().minimumRedeemPoints
    return if (program.configuration.couponEnabled) {
        program.configuration.couponRule.pointsCost.coerceAtLeast(1)
    } else {
        program.configuration.pointsRule.minimumRedeemPoints.coerceAtLeast(1)
    }
}

internal fun List<RewardProgram>.calculateCashbackCredit(amount: Double): Int {
    if (amount <= 0.0) return 0
    val rule = resolveProgramFor(RewardProgramScanAction.APPLY_CASHBACK)?.configuration?.cashbackRule
        ?: return ((amount * 0.05).roundToInt()).coerceAtLeast(1)
    if (amount < rule.minimumSpendAmount) return 0
    return ((amount * rule.cashbackPercent / 100.0).roundToInt()).coerceAtLeast(1)
}

internal fun List<RewardProgram>.cashbackMinimumSpendAmount(): Double =
    resolveProgramFor(RewardProgramScanAction.APPLY_CASHBACK)?.configuration?.cashbackRule?.minimumSpendAmount ?: 0.0

internal fun List<RewardProgram>.checkInRewardPoints(): Int =
    (resolveProgramFor(RewardProgramScanAction.CHECK_IN)?.configuration?.checkInRule ?: CheckInProgramRule()).rewardPoints

private fun RewardProgramScanAction.isEnabledIn(configuration: RewardProgramConfiguration): Boolean = when (this) {
    RewardProgramScanAction.EARN_POINTS -> configuration.earningEnabled
    RewardProgramScanAction.REDEEM_REWARDS -> configuration.rewardRedemptionEnabled
    RewardProgramScanAction.CHECK_IN -> configuration.visitCheckInEnabled
    RewardProgramScanAction.APPLY_CASHBACK -> configuration.cashbackEnabled
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> configuration.tierTrackingEnabled
}

private fun RewardProgram.priorityFor(action: RewardProgramScanAction): Int = when (action) {
    RewardProgramScanAction.EARN_POINTS -> when (type) {
        LoyaltyProgramType.POINTS -> 0
        LoyaltyProgramType.TIER -> 1
        LoyaltyProgramType.HYBRID -> 2
        LoyaltyProgramType.PURCHASE_FREQUENCY -> 3
        else -> 9
    }
    RewardProgramScanAction.REDEEM_REWARDS -> when (type) {
        LoyaltyProgramType.COUPON -> 0
        LoyaltyProgramType.POINTS -> 1
        LoyaltyProgramType.HYBRID -> 2
        else -> 9
    }
    RewardProgramScanAction.CHECK_IN -> when (type) {
        LoyaltyProgramType.DIGITAL_STAMP -> 0
        LoyaltyProgramType.HYBRID -> 1
        else -> 9
    }
    RewardProgramScanAction.APPLY_CASHBACK -> when (type) {
        LoyaltyProgramType.CASHBACK -> 0
        else -> 9
    }
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> when (type) {
        LoyaltyProgramType.TIER -> 0
        LoyaltyProgramType.HYBRID -> 1
        else -> 9
    }
}
