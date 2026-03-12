package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.loyalty.CashbackProgramRule
import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.loyalty.CouponProgramRule
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.PurchaseFrequencyProgramRule
import com.vector.verevcodex.domain.model.loyalty.ReferralProgramRule
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule

fun defaultProgramEditorState(type: LoyaltyProgramType = LoyaltyProgramType.POINTS): ProgramEditorState {
    val configuration = RewardProgramConfigurationFactory.defaultFor(type, active = true)
    return ProgramEditorState(
        type = type,
        pointsSpendStepAmount = configuration.pointsRule.spendStepAmount.toString(),
        pointsAwardedPerStep = configuration.pointsRule.pointsAwardedPerStep.toString(),
        pointsWelcomeBonus = configuration.pointsRule.welcomeBonusPoints.toString(),
        pointsMinimumRedeem = configuration.pointsRule.minimumRedeemPoints.toString(),
        cashbackPercent = decimalString(configuration.cashbackRule.cashbackPercent),
        cashbackMinimumSpendAmount = decimalString(configuration.cashbackRule.minimumSpendAmount),
        tierSilverThreshold = configuration.tierRule.silverThreshold.toString(),
        tierGoldThreshold = configuration.tierRule.goldThreshold.toString(),
        tierVipThreshold = configuration.tierRule.vipThreshold.toString(),
        tierBonusPercent = configuration.tierRule.tierBonusPercent.toString(),
        couponName = configuration.couponRule.couponName,
        couponPointsCost = configuration.couponRule.pointsCost.toString(),
        couponDiscountAmount = decimalString(configuration.couponRule.discountAmount),
        couponMinimumSpendAmount = decimalString(configuration.couponRule.minimumSpendAmount),
        checkInVisitsRequired = configuration.checkInRule.visitsRequired.toString(),
        checkInRewardPoints = configuration.checkInRule.rewardPoints.toString(),
        checkInRewardName = configuration.checkInRule.rewardName,
        purchaseFrequencyCount = configuration.purchaseFrequencyRule.purchaseCount.toString(),
        purchaseFrequencyWindowDays = configuration.purchaseFrequencyRule.windowDays.toString(),
        purchaseFrequencyRewardPoints = configuration.purchaseFrequencyRule.rewardPoints.toString(),
        purchaseFrequencyRewardName = configuration.purchaseFrequencyRule.rewardName,
        referralReferrerRewardPoints = configuration.referralRule.referrerRewardPoints.toString(),
        referralRefereeRewardPoints = configuration.referralRule.refereeRewardPoints.toString(),
        referralCodePrefix = configuration.referralRule.referralCodePrefix,
    )
}

fun RewardProgram.toEditorState(): ProgramEditorState = ProgramEditorState(
    programId = id,
    name = name,
    description = description,
    type = type,
    active = active,
    pointsSpendStepAmount = configuration.pointsRule.spendStepAmount.toString(),
    pointsAwardedPerStep = configuration.pointsRule.pointsAwardedPerStep.toString(),
    pointsWelcomeBonus = configuration.pointsRule.welcomeBonusPoints.toString(),
    pointsMinimumRedeem = configuration.pointsRule.minimumRedeemPoints.toString(),
    cashbackPercent = decimalString(configuration.cashbackRule.cashbackPercent),
    cashbackMinimumSpendAmount = decimalString(configuration.cashbackRule.minimumSpendAmount),
    tierSilverThreshold = configuration.tierRule.silverThreshold.toString(),
    tierGoldThreshold = configuration.tierRule.goldThreshold.toString(),
    tierVipThreshold = configuration.tierRule.vipThreshold.toString(),
    tierBonusPercent = configuration.tierRule.tierBonusPercent.toString(),
    couponName = configuration.couponRule.couponName,
    couponPointsCost = configuration.couponRule.pointsCost.toString(),
    couponDiscountAmount = decimalString(configuration.couponRule.discountAmount),
    couponMinimumSpendAmount = decimalString(configuration.couponRule.minimumSpendAmount),
    checkInVisitsRequired = configuration.checkInRule.visitsRequired.toString(),
    checkInRewardPoints = configuration.checkInRule.rewardPoints.toString(),
    checkInRewardName = configuration.checkInRule.rewardName,
    purchaseFrequencyCount = configuration.purchaseFrequencyRule.purchaseCount.toString(),
    purchaseFrequencyWindowDays = configuration.purchaseFrequencyRule.windowDays.toString(),
    purchaseFrequencyRewardPoints = configuration.purchaseFrequencyRule.rewardPoints.toString(),
    purchaseFrequencyRewardName = configuration.purchaseFrequencyRule.rewardName,
    referralReferrerRewardPoints = configuration.referralRule.referrerRewardPoints.toString(),
    referralRefereeRewardPoints = configuration.referralRule.refereeRewardPoints.toString(),
    referralCodePrefix = configuration.referralRule.referralCodePrefix,
)

fun ProgramEditorState.toDraft(storeId: String): RewardProgramDraft {
    val configuration = toConfiguration()
    return RewardProgramDraft(
        storeId = storeId,
        name = name.trim(),
        description = description.trim(),
        type = type,
        rulesSummary = buildRulesSummary(configuration),
        active = active,
        configuration = configuration,
    )
}

fun ProgramEditorState.validate(): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (name.isBlank()) errors[PROGRAM_FIELD_NAME] = R.string.merchant_program_error_name_required
    if (description.isBlank()) errors[PROGRAM_FIELD_DESCRIPTION] = R.string.merchant_program_error_description_required
    when (type) {
        LoyaltyProgramType.POINTS -> {
            if (positiveInt(pointsSpendStepAmount) == null) errors[PROGRAM_FIELD_POINTS_STEP] = R.string.merchant_program_error_positive_required
            if (positiveInt(pointsAwardedPerStep) == null) errors[PROGRAM_FIELD_POINTS_AWARDED] = R.string.merchant_program_error_positive_required
            if (positiveInt(pointsMinimumRedeem) == null) errors[PROGRAM_FIELD_POINTS_REDEEM] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.DIGITAL_STAMP -> {
            if (positiveInt(checkInVisitsRequired) == null) errors[PROGRAM_FIELD_CHECKIN_VISITS] = R.string.merchant_program_error_positive_required
            if (positiveInt(checkInRewardPoints) == null) errors[PROGRAM_FIELD_CHECKIN_REWARD] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.TIER -> {
            val silver = positiveInt(tierSilverThreshold)
            val gold = positiveInt(tierGoldThreshold)
            val vip = positiveInt(tierVipThreshold)
            if (silver == null) errors[PROGRAM_FIELD_TIER_SILVER] = R.string.merchant_program_error_positive_required
            if (gold == null) errors[PROGRAM_FIELD_TIER_GOLD] = R.string.merchant_program_error_positive_required
            if (vip == null) errors[PROGRAM_FIELD_TIER_VIP] = R.string.merchant_program_error_positive_required
            if (silver != null && gold != null && gold <= silver) errors[PROGRAM_FIELD_TIER_GOLD] = R.string.merchant_program_error_tier_order
            if (gold != null && vip != null && vip <= gold) errors[PROGRAM_FIELD_TIER_VIP] = R.string.merchant_program_error_tier_order
        }
        LoyaltyProgramType.COUPON -> {
            if (couponName.isBlank()) errors[PROGRAM_FIELD_COUPON_NAME] = R.string.merchant_program_error_coupon_name_required
            if (positiveInt(couponPointsCost) == null) errors[PROGRAM_FIELD_COUPON_POINTS] = R.string.merchant_program_error_positive_required
            if (positiveDecimal(couponDiscountAmount) == null) errors[PROGRAM_FIELD_COUPON_DISCOUNT] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.PURCHASE_FREQUENCY -> {
            if (positiveInt(purchaseFrequencyCount) == null) errors[PROGRAM_FIELD_FREQUENCY_COUNT] = R.string.merchant_program_error_positive_required
            if (positiveInt(purchaseFrequencyWindowDays) == null) errors[PROGRAM_FIELD_FREQUENCY_WINDOW] = R.string.merchant_program_error_positive_required
            if (positiveInt(purchaseFrequencyRewardPoints) == null) errors[PROGRAM_FIELD_FREQUENCY_REWARD] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.REFERRAL -> {
            if (positiveInt(referralReferrerRewardPoints) == null) errors[PROGRAM_FIELD_REFERRAL_REFERRER] = R.string.merchant_program_error_positive_required
            if (positiveInt(referralRefereeRewardPoints) == null) errors[PROGRAM_FIELD_REFERRAL_REFEREE] = R.string.merchant_program_error_positive_required
            if (referralCodePrefix.trim().length < 2) errors[PROGRAM_FIELD_REFERRAL_PREFIX] = R.string.merchant_program_error_referral_prefix
        }
        LoyaltyProgramType.HYBRID -> {
            if (positiveInt(pointsSpendStepAmount) == null) errors[PROGRAM_FIELD_POINTS_STEP] = R.string.merchant_program_error_positive_required
            if (positiveInt(checkInVisitsRequired) == null) errors[PROGRAM_FIELD_CHECKIN_VISITS] = R.string.merchant_program_error_positive_required
            if (positiveInt(referralReferrerRewardPoints) == null) errors[PROGRAM_FIELD_REFERRAL_REFERRER] = R.string.merchant_program_error_positive_required
        }
    }
    return errors
}

fun ProgramEditorState.toConfiguration(): RewardProgramConfiguration {
    val baseConfiguration = RewardProgramConfigurationFactory.defaultFor(type = type, active = active)
    val pointsRule = PointsProgramRule(
        spendStepAmount = positiveInt(pointsSpendStepAmount) ?: baseConfiguration.pointsRule.spendStepAmount,
        pointsAwardedPerStep = positiveInt(pointsAwardedPerStep) ?: baseConfiguration.pointsRule.pointsAwardedPerStep,
        welcomeBonusPoints = nonNegativeInt(pointsWelcomeBonus) ?: baseConfiguration.pointsRule.welcomeBonusPoints,
        minimumRedeemPoints = positiveInt(pointsMinimumRedeem) ?: baseConfiguration.pointsRule.minimumRedeemPoints,
    )
    val cashbackRule = CashbackProgramRule(
        cashbackPercent = positiveDecimal(cashbackPercent) ?: baseConfiguration.cashbackRule.cashbackPercent,
        minimumSpendAmount = nonNegativeDecimal(cashbackMinimumSpendAmount) ?: baseConfiguration.cashbackRule.minimumSpendAmount,
    )
    val tierRule = TierProgramRule(
        silverThreshold = positiveInt(tierSilverThreshold) ?: baseConfiguration.tierRule.silverThreshold,
        goldThreshold = positiveInt(tierGoldThreshold) ?: baseConfiguration.tierRule.goldThreshold,
        vipThreshold = positiveInt(tierVipThreshold) ?: baseConfiguration.tierRule.vipThreshold,
        tierBonusPercent = positiveInt(tierBonusPercent) ?: baseConfiguration.tierRule.tierBonusPercent,
    )
    val couponRule = CouponProgramRule(
        couponName = couponName.trim().ifBlank { baseConfiguration.couponRule.couponName },
        pointsCost = positiveInt(couponPointsCost) ?: baseConfiguration.couponRule.pointsCost,
        discountAmount = positiveDecimal(couponDiscountAmount) ?: baseConfiguration.couponRule.discountAmount,
        minimumSpendAmount = nonNegativeDecimal(couponMinimumSpendAmount) ?: baseConfiguration.couponRule.minimumSpendAmount,
    )
    val checkInRule = CheckInProgramRule(
        visitsRequired = positiveInt(checkInVisitsRequired) ?: baseConfiguration.checkInRule.visitsRequired,
        rewardPoints = positiveInt(checkInRewardPoints) ?: baseConfiguration.checkInRule.rewardPoints,
        rewardName = checkInRewardName.trim().ifBlank { baseConfiguration.checkInRule.rewardName },
    )
    val purchaseFrequencyRule = PurchaseFrequencyProgramRule(
        purchaseCount = positiveInt(purchaseFrequencyCount) ?: baseConfiguration.purchaseFrequencyRule.purchaseCount,
        windowDays = positiveInt(purchaseFrequencyWindowDays) ?: baseConfiguration.purchaseFrequencyRule.windowDays,
        rewardPoints = positiveInt(purchaseFrequencyRewardPoints) ?: baseConfiguration.purchaseFrequencyRule.rewardPoints,
        rewardName = purchaseFrequencyRewardName.trim().ifBlank { baseConfiguration.purchaseFrequencyRule.rewardName },
    )
    val referralRule = ReferralProgramRule(
        referrerRewardPoints = positiveInt(referralReferrerRewardPoints) ?: baseConfiguration.referralRule.referrerRewardPoints,
        refereeRewardPoints = positiveInt(referralRefereeRewardPoints) ?: baseConfiguration.referralRule.refereeRewardPoints,
        referralCodePrefix = referralCodePrefix.trim().uppercase().ifBlank { baseConfiguration.referralRule.referralCodePrefix },
    )

    return when (type) {
        LoyaltyProgramType.POINTS -> RewardProgramConfiguration(
            earningEnabled = true,
            rewardRedemptionEnabled = true,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            scanActions = if (active) setOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.REDEEM_REWARDS) else emptySet(),
            pointsRule = pointsRule,
        )
        LoyaltyProgramType.DIGITAL_STAMP -> RewardProgramConfiguration(
            earningEnabled = false,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = true,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            scanActions = if (active) setOf(RewardProgramScanAction.CHECK_IN) else emptySet(),
            checkInRule = checkInRule,
        )
        LoyaltyProgramType.TIER -> RewardProgramConfiguration(
            earningEnabled = true,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = true,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            scanActions = if (active) setOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.TRACK_TIER_PROGRESS) else emptySet(),
            pointsRule = pointsRule,
            tierRule = tierRule,
        )
        LoyaltyProgramType.COUPON -> RewardProgramConfiguration(
            earningEnabled = false,
            rewardRedemptionEnabled = true,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = true,
            purchaseFrequencyEnabled = false,
            referralEnabled = false,
            scanActions = if (active) setOf(RewardProgramScanAction.REDEEM_REWARDS) else emptySet(),
            couponRule = couponRule,
        )
        LoyaltyProgramType.PURCHASE_FREQUENCY -> RewardProgramConfiguration(
            earningEnabled = true,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = true,
            referralEnabled = false,
            scanActions = if (active) setOf(RewardProgramScanAction.EARN_POINTS) else emptySet(),
            purchaseFrequencyRule = purchaseFrequencyRule,
        )
        LoyaltyProgramType.REFERRAL -> RewardProgramConfiguration(
            earningEnabled = false,
            rewardRedemptionEnabled = false,
            visitCheckInEnabled = false,
            cashbackEnabled = false,
            tierTrackingEnabled = false,
            couponEnabled = false,
            purchaseFrequencyEnabled = false,
            referralEnabled = true,
            scanActions = emptySet(),
            referralRule = referralRule,
        )
        LoyaltyProgramType.HYBRID -> RewardProgramConfiguration(
            earningEnabled = true,
            rewardRedemptionEnabled = true,
            visitCheckInEnabled = true,
            cashbackEnabled = true,
            tierTrackingEnabled = true,
            couponEnabled = true,
            purchaseFrequencyEnabled = true,
            referralEnabled = true,
            scanActions = if (active) setOf(
                RewardProgramScanAction.EARN_POINTS,
                RewardProgramScanAction.REDEEM_REWARDS,
                RewardProgramScanAction.CHECK_IN,
                RewardProgramScanAction.APPLY_CASHBACK,
                RewardProgramScanAction.TRACK_TIER_PROGRESS,
            ) else emptySet(),
            pointsRule = pointsRule,
            cashbackRule = cashbackRule,
            tierRule = tierRule,
            couponRule = couponRule,
            checkInRule = checkInRule,
            purchaseFrequencyRule = purchaseFrequencyRule,
            referralRule = referralRule,
        )
    }
}

fun buildRulesSummary(configuration: RewardProgramConfiguration): String = when {
    configuration.referralEnabled -> "${configuration.referralRule.referrerRewardPoints} pts referrer / ${configuration.referralRule.refereeRewardPoints} pts referee"
    configuration.purchaseFrequencyEnabled -> "${configuration.purchaseFrequencyRule.purchaseCount} purchases in ${configuration.purchaseFrequencyRule.windowDays} days"
    configuration.couponEnabled -> "${configuration.couponRule.couponName} for ${configuration.couponRule.pointsCost} pts"
    configuration.tierTrackingEnabled -> "Silver ${configuration.tierRule.silverThreshold}, Gold ${configuration.tierRule.goldThreshold}, VIP ${configuration.tierRule.vipThreshold}"
    configuration.visitCheckInEnabled -> "Reward after ${configuration.checkInRule.visitsRequired} check-ins"
    configuration.cashbackEnabled -> "${decimalString(configuration.cashbackRule.cashbackPercent)}% cashback"
    else -> "${configuration.pointsRule.pointsAwardedPerStep} point per ${configuration.pointsRule.spendStepAmount} AMD"
}

private fun positiveInt(value: String): Int? = value.trim().toIntOrNull()?.takeIf { it > 0 }
private fun nonNegativeInt(value: String): Int? = value.trim().toIntOrNull()?.takeIf { it >= 0 }
private fun positiveDecimal(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it > 0.0 }
private fun nonNegativeDecimal(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it >= 0.0 }

private fun decimalString(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
