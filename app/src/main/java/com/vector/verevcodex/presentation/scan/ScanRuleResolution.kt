package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.displayValue
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction

internal fun List<RewardProgram>.resolveProgramFor(action: RewardProgramScanAction): RewardProgram? =
    asSequence()
        .filter { program ->
            program.active &&
                action in program.configuration.scanActions &&
                action.isEnabledIn(program.configuration)
        }
        .sortedBy { program -> program.priorityFor(action) }
        .firstOrNull()

internal fun List<RewardProgram>.calculateEarnedPoints(amount: Double, customer: Customer): Int {
    if (amount <= 0.0) return 0
    val rule = resolveProgramFor(RewardProgramScanAction.EARN_POINTS)?.configuration?.pointsRule ?: PointsProgramRule()
    val spendStep = rule.spendStepAmount.coerceAtLeast(1)
    val stepCount = (amount / spendStep).toInt()
    if (stepCount <= 0 || rule.pointsAwardedPerStep <= 0) return 0
    val basePoints = stepCount * rule.pointsAwardedPerStep
    val tierBonusPercent = activeTierRule()?.activeBonusPercent(customer.currentPoints, customer.totalSpent) ?: 0
    return basePoints + ((basePoints * tierBonusPercent) / 100)
}

internal fun List<RewardProgram>.minimumRedeemPoints(): Int {
    val program = resolveProgramFor(RewardProgramScanAction.REDEEM_REWARDS) ?: return PointsProgramRule().minimumRedeemPoints
    return if (program.configuration.couponEnabled) {
        program.configuration.couponRule.pointsCost.coerceAtLeast(1)
    } else {
        program.configuration.pointsRule.minimumRedeemPoints.coerceAtLeast(1)
    }
}

internal fun List<RewardProgram>.checkInRewardSummary(): String =
    (resolveProgramFor(RewardProgramScanAction.CHECK_IN)?.configuration?.checkInRule ?: CheckInProgramRule()).rewardOutcome.displayValue()

internal fun List<RewardProgram>.currentTierDiscountPercent(customer: Customer): Int =
    activeTierRule()?.activeDiscountPercent(customer.currentPoints, customer.totalSpent) ?: 0

internal fun List<RewardProgram>.discountedTotal(amount: Double, customer: Customer): Double {
    val discountPercent = currentTierDiscountPercent(customer)
    if (discountPercent <= 0) return amount
    return amount * (1.0 - (discountPercent / 100.0))
}

private fun List<RewardProgram>.activeTierRule() =
    firstOrNull { it.active && it.configuration.tierTrackingEnabled }?.configuration?.tierRule

private fun RewardProgramScanAction.isEnabledIn(configuration: RewardProgramConfiguration): Boolean = when (this) {
    RewardProgramScanAction.EARN_POINTS -> configuration.earningEnabled
    RewardProgramScanAction.REDEEM_REWARDS -> configuration.rewardRedemptionEnabled
    RewardProgramScanAction.CHECK_IN -> configuration.visitCheckInEnabled
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> configuration.tierTrackingEnabled
}

private fun RewardProgram.priorityFor(action: RewardProgramScanAction): Int = when (action) {
    RewardProgramScanAction.EARN_POINTS -> when (type) {
        LoyaltyProgramType.POINTS -> 0
        LoyaltyProgramType.TIER -> 1
        LoyaltyProgramType.PURCHASE_FREQUENCY -> 2
        else -> 9
    }
    RewardProgramScanAction.REDEEM_REWARDS -> when (type) {
        LoyaltyProgramType.POINTS -> 0
        else -> 9
    }
    RewardProgramScanAction.CHECK_IN -> when (type) {
        LoyaltyProgramType.DIGITAL_STAMP -> 0
        else -> 9
    }
    RewardProgramScanAction.TRACK_TIER_PROGRESS -> when (type) {
        LoyaltyProgramType.TIER -> 0
        else -> 9
    }
}
