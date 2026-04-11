package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.common.CouponBenefitType
import com.vector.verevcodex.domain.model.common.RewardCatalogType
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.TierBenefitType
import com.vector.verevcodex.domain.model.loyalty.displayValue
import kotlin.math.min
import kotlin.math.roundToInt

internal enum class ScanSpendMode {
    POINTS,
    COUPON,
}

internal data class ScanProjectedBenefit(
    val title: String,
    val detail: String,
)

internal data class ScanCheckoutPreview(
    val purchaseAmount: Double,
    val checkoutAvailable: Boolean,
    val tierDiscountPercent: Int,
    val tierDiscountAmount: Double,
    val couponDiscountPercent: Int,
    val couponDiscountAmount: Double,
    val amountAfterDiscounts: Double,
    val maxRedeemablePoints: Int,
    val appliedRedeemPoints: Int,
    val selectedCoupon: Reward? = null,
    val rewardPointsCost: Int = 0,
    val finalAmount: Double,
    val basePoints: Int,
    val welcomeBonusPoints: Int,
    val tierBonusPoints: Int,
    val totalEarnedPoints: Int,
    val projectedPointsBalance: Int,
    val currentTierLabel: String,
    val projectedTierLabel: String,
    val projectedBenefits: List<ScanProjectedBenefit>,
    val minimumRedeemPoints: Int,
    val canUseBenefits: Boolean,
)

internal fun List<Reward>.availableScanCheckoutCoupons(
    customer: Customer,
    activePrograms: List<RewardProgram>,
    today: java.time.LocalDate = java.time.LocalDate.now(),
): List<Reward> {
    if (activePrograms.resolveProgramFor(com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction.REDEEM_REWARDS) == null) {
        return emptyList()
    }
    return filter { reward ->
        reward.activeStatus &&
            reward.catalogType == RewardCatalogType.COUPON &&
            reward.couponBenefitType == CouponBenefitType.DISCOUNT_PERCENT &&
            reward.pointsRequired > 0 &&
            customer.currentPoints >= reward.pointsRequired &&
            (reward.expirationDate == null || !reward.expirationDate.isBefore(today))
    }.sortedWith(
        compareByDescending<Reward> { it.couponDiscountPercent ?: 0.0 }
            .thenBy { it.pointsRequired }
            .thenBy { it.name },
    )
}

internal fun computeScanCheckoutPreview(
    customer: Customer,
    activePrograms: List<RewardProgram>,
    availableCoupons: List<Reward>,
    amountInput: String,
    useBenefits: Boolean,
    spendMode: ScanSpendMode,
    pointsInput: String,
    selectedCouponId: String?,
): ScanCheckoutPreview {
    val purchaseAmount = amountInput.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 0.0
    val pointsProgram = activePrograms.resolveProgramFor(com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction.EARN_POINTS)
    val tierProgram = activePrograms.firstOrNull { it.active && it.configuration.tierTrackingEnabled }
    val redeemProgram = activePrograms.resolveProgramFor(com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction.REDEEM_REWARDS)
    val checkoutAvailable = pointsProgram != null || tierProgram != null || redeemProgram != null

    val tierDiscountPercent = activePrograms.currentTierDiscountPercent(customer)
    val tierDiscountAmount = if (purchaseAmount > 0.0 && tierDiscountPercent > 0) {
        purchaseAmount * (tierDiscountPercent / 100.0)
    } else {
        0.0
    }
    val selectedCoupon = availableCoupons.firstOrNull { it.id == selectedCouponId }
        ?.takeIf { useBenefits && spendMode == ScanSpendMode.COUPON }
    val couponDiscountPercent = selectedCoupon?.couponDiscountPercent?.roundToInt()?.coerceAtLeast(0) ?: 0
    val amountAfterTierDiscount = (purchaseAmount - tierDiscountAmount).coerceAtLeast(0.0)
    val couponDiscountAmount = if (amountAfterTierDiscount > 0.0 && couponDiscountPercent > 0) {
        amountAfterTierDiscount * (couponDiscountPercent / 100.0)
    } else {
        0.0
    }
    val amountAfterDiscounts = (amountAfterTierDiscount - couponDiscountAmount).coerceAtLeast(0.0)

    val maxRedeemablePoints = min(
        customer.currentPoints,
        amountAfterDiscounts.roundToInt().coerceAtLeast(0),
    )
    val minimumRedeemPoints = activePrograms.minimumRedeemPoints()
    val requestedRedeemPoints = pointsInput.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val appliedRedeemPoints = if (useBenefits && spendMode == ScanSpendMode.POINTS) {
        requestedRedeemPoints.coerceAtMost(maxRedeemablePoints).takeIf { it >= minimumRedeemPoints } ?: 0
    } else {
        0
    }
    val rewardPointsCost = selectedCoupon?.pointsRequired ?: 0
    val finalAmount = (amountAfterDiscounts - appliedRedeemPoints).coerceAtLeast(0.0)

    val basePoints = activePrograms.calculateBaseEarnedPoints(finalAmount)
    val welcomeBonusPoints = if (basePoints > 0 && customer.totalVisits == 0) {
        pointsProgram?.configuration?.pointsRule?.welcomeBonusPoints?.coerceAtLeast(0) ?: 0
    } else {
        0
    }
    val tierBonusPercent = tierProgram?.configuration?.tierRule?.activeBonusPercent(
        points = customer.currentPoints,
        totalSpent = customer.totalSpent,
    ) ?: 0
    val tierBonusPoints = if (basePoints > 0 && tierBonusPercent > 0) {
        ((basePoints * tierBonusPercent) / 100.0).toInt()
    } else {
        0
    }
    val totalEarnedPoints = basePoints + welcomeBonusPoints + tierBonusPoints
    val totalSpentPoints = appliedRedeemPoints + rewardPointsCost
    val projectedPointsBalance = customer.currentPoints - totalSpentPoints + totalEarnedPoints

    val currentTierLabel = tierProgram?.configuration?.tierRule
        ?.earnedLevelFor(customer.currentPoints, customer.totalSpent)
        ?.name
        ?: customer.loyaltyTierLabel
    val projectedTierLevel = tierProgram?.configuration?.tierRule?.earnedLevelFor(
        points = projectedPointsBalance,
        totalSpent = customer.totalSpent + finalAmount,
    )
    val projectedTierLabel = projectedTierLevel?.name ?: currentTierLabel

    val projectedBenefits = buildProjectedBenefits(
        customer = customer,
        totalEarnedPoints = totalEarnedPoints,
        welcomeBonusPoints = welcomeBonusPoints,
        tierBonusPoints = tierBonusPoints,
        tierProgram = tierProgram,
        projectedPointsBalance = projectedPointsBalance,
        projectedTotalSpent = customer.totalSpent + finalAmount,
    )

    return ScanCheckoutPreview(
        purchaseAmount = purchaseAmount,
        checkoutAvailable = checkoutAvailable,
        tierDiscountPercent = tierDiscountPercent,
        tierDiscountAmount = tierDiscountAmount,
        couponDiscountPercent = couponDiscountPercent,
        couponDiscountAmount = couponDiscountAmount,
        amountAfterDiscounts = amountAfterDiscounts,
        maxRedeemablePoints = maxRedeemablePoints,
        appliedRedeemPoints = appliedRedeemPoints,
        selectedCoupon = selectedCoupon,
        rewardPointsCost = rewardPointsCost,
        finalAmount = finalAmount,
        basePoints = basePoints,
        welcomeBonusPoints = welcomeBonusPoints,
        tierBonusPoints = tierBonusPoints,
        totalEarnedPoints = totalEarnedPoints,
        projectedPointsBalance = projectedPointsBalance,
        currentTierLabel = currentTierLabel,
        projectedTierLabel = projectedTierLabel,
        projectedBenefits = projectedBenefits,
        minimumRedeemPoints = minimumRedeemPoints,
        canUseBenefits = redeemProgram != null && (customer.currentPoints > 0 || availableCoupons.isNotEmpty()),
    )
}

internal fun ScanCheckoutPreview.totalSpentPoints(): Int = appliedRedeemPoints + rewardPointsCost

internal fun ScanCheckoutPreview.hasTierChange(): Boolean =
    currentTierLabel.isNotBlank() && projectedTierLabel.isNotBlank() && !currentTierLabel.equals(projectedTierLabel, ignoreCase = true)

internal fun ScanCheckoutPreview.supportingPointsText(): String = buildString {
    if (totalEarnedPoints > 0) {
        append("+")
        append(totalEarnedPoints)
        append(" pts")
    } else {
        append("0 pts")
    }
}

internal fun List<RewardProgram>.calculateBaseEarnedPoints(amount: Double): Int {
    if (amount <= 0.0) return 0
    val rule = resolveProgramFor(com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction.EARN_POINTS)
        ?.configuration
        ?.pointsRule
        ?: return 0
    val spendStep = rule.spendStepAmount.coerceAtLeast(1)
    val stepCount = (amount / spendStep).toInt()
    if (stepCount <= 0 || rule.pointsAwardedPerStep <= 0) return 0
    return stepCount * rule.pointsAwardedPerStep
}

private fun buildProjectedBenefits(
    customer: Customer,
    totalEarnedPoints: Int,
    welcomeBonusPoints: Int,
    tierBonusPoints: Int,
    tierProgram: RewardProgram?,
    projectedPointsBalance: Int,
    projectedTotalSpent: Double,
): List<ScanProjectedBenefit> {
    val projected = mutableListOf<ScanProjectedBenefit>()
    if (welcomeBonusPoints > 0) {
        projected += ScanProjectedBenefit(
            title = "Welcome bonus",
            detail = "+$welcomeBonusPoints pts on this purchase",
        )
    }
    if (tierBonusPoints > 0) {
        projected += ScanProjectedBenefit(
            title = "Tier bonus",
            detail = "+$tierBonusPoints pts from current tier benefits",
        )
    }

    val tierRule = tierProgram?.configuration?.tierRule ?: return projected
    val currentLevel = tierRule.earnedLevelFor(customer.currentPoints, customer.totalSpent)
    val projectedLevel = tierRule.earnedLevelFor(projectedPointsBalance, projectedTotalSpent)
    if (projectedLevel != null && projectedLevel.id != currentLevel?.id) {
        projected += ScanProjectedBenefit(
            title = "Tier unlock",
            detail = buildTierBenefitDetail(projectedLevel, totalEarnedPoints),
        )
    } else if (projected.isEmpty() && totalEarnedPoints > 0) {
        projected += ScanProjectedBenefit(
            title = "Estimated points",
            detail = "+$totalEarnedPoints pts after checkout",
        )
    }
    return projected
}

private fun buildTierBenefitDetail(level: com.vector.verevcodex.domain.model.loyalty.TierLevelRule, totalEarnedPoints: Int): String {
    val rewardDetail = level.rewardOutcome.displayValue().takeIf { it.isNotBlank() }
    if (rewardDetail != null) {
        return "${level.name} unlocked • $rewardDetail"
    }
    return when (level.benefitType) {
        TierBenefitType.DISCOUNT_PERCENT -> "${level.name} unlocked • ${level.bonusPercent}% automatic discount"
        TierBenefitType.BONUS_PERCENT -> "${level.name} unlocked • ${level.bonusPercent}% bonus earn rate"
    }
}
