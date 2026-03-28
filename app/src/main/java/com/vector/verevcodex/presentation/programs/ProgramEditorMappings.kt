package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.loyalty.CashbackProgramRule
import com.vector.verevcodex.domain.model.loyalty.CheckInProgramRule
import com.vector.verevcodex.domain.model.loyalty.CouponProgramRule
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.RewardType
import com.vector.verevcodex.domain.model.loyalty.PointsProgramRule
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcome
import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.PurchaseFrequencyProgramRule
import com.vector.verevcodex.domain.model.loyalty.ReferralProgramRule
import com.vector.verevcodex.domain.model.loyalty.Reward
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.RewardProgramDraft
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.loyalty.TierLevelRule
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule
import com.vector.verevcodex.domain.model.loyalty.impliedRewardType
import com.vector.verevcodex.domain.model.loyalty.usesProgramBenefit
import com.vector.verevcodex.domain.model.loyalty.usesRewardItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun defaultProgramEditorState(type: LoyaltyProgramType = LoyaltyProgramType.POINTS): ProgramEditorState {
    val configuration = RewardProgramConfigurationFactory.defaultFor(type, active = true)
    return ProgramEditorState(
        type = type,
        scheduleStartDate = "",
        scheduleEndDate = "",
        pointsSpendStepAmount = configuration.pointsRule.spendStepAmount.toString(),
        pointsAwardedPerStep = configuration.pointsRule.pointsAwardedPerStep.toString(),
        pointsWelcomeBonus = "",
        pointsMinimumRedeem = configuration.pointsRule.minimumRedeemPoints.toString(),
        cashbackPercent = decimalString(configuration.cashbackRule.cashbackPercent),
        cashbackMinimumSpendAmount = "",
        tierLevels = configuration.tierRule.sortedLevels.toEditorState().map { level ->
            level.copy(
                bonusPercent = "0",
                rewardOutcome = ProgramRewardOutcomeEditorState(),
            )
        },
        couponName = "",
        couponPointsCost = configuration.couponRule.pointsCost.toString(),
        couponDiscountAmount = decimalString(configuration.couponRule.discountAmount),
        couponMinimumSpendAmount = "",
        checkInVisitsRequired = configuration.checkInRule.visitsRequired.toString(),
        checkInReward = ProgramRewardOutcomeEditorState(),
        purchaseFrequencyCount = configuration.purchaseFrequencyRule.purchaseCount.toString(),
        purchaseFrequencyWindowDays = configuration.purchaseFrequencyRule.windowDays.toString(),
        purchaseFrequencyReward = ProgramRewardOutcomeEditorState(),
        referralReferrerReward = ProgramRewardOutcomeEditorState(),
        referralRefereeReward = ProgramRewardOutcomeEditorState(),
        referralCodePrefix = "",
    )
}

fun RewardProgram.toEditorState(): ProgramEditorState = ProgramEditorState(
    programId = id,
    name = name,
    description = description,
    type = type,
    active = active,
    autoScheduleEnabled = autoScheduleEnabled,
    scheduleStartDate = scheduleStartDate?.toString().orEmpty(),
    scheduleEndDate = scheduleEndDate?.toString().orEmpty(),
    annualRepeatEnabled = annualRepeatEnabled,
    pointsSpendStepAmount = configuration.pointsRule.spendStepAmount.toString(),
    pointsAwardedPerStep = configuration.pointsRule.pointsAwardedPerStep.toString(),
    pointsWelcomeBonus = configuration.pointsRule.welcomeBonusPoints.toString(),
    pointsMinimumRedeem = configuration.pointsRule.minimumRedeemPoints.toString(),
    cashbackPercent = decimalString(configuration.cashbackRule.cashbackPercent),
    cashbackMinimumSpendAmount = decimalString(configuration.cashbackRule.minimumSpendAmount),
    tierLevels = configuration.tierRule.sortedLevels.toEditorState(),
    couponName = configuration.couponRule.couponName,
    couponPointsCost = configuration.couponRule.pointsCost.toString(),
    couponDiscountAmount = decimalString(configuration.couponRule.discountAmount),
    couponMinimumSpendAmount = decimalString(configuration.couponRule.minimumSpendAmount),
    checkInVisitsRequired = configuration.checkInRule.visitsRequired.toString(),
    checkInReward = configuration.checkInRule.rewardOutcome.toEditorState(
        fallbackPoints = configuration.checkInRule.rewardPoints,
        fallbackLabel = configuration.checkInRule.rewardName,
    ),
    purchaseFrequencyCount = configuration.purchaseFrequencyRule.purchaseCount.toString(),
    purchaseFrequencyWindowDays = configuration.purchaseFrequencyRule.windowDays.toString(),
    purchaseFrequencyReward = configuration.purchaseFrequencyRule.rewardOutcome.toEditorState(
        fallbackPoints = configuration.purchaseFrequencyRule.rewardPoints,
        fallbackLabel = configuration.purchaseFrequencyRule.rewardName,
    ),
    referralReferrerReward = configuration.referralRule.referrerRewardOutcome.toEditorState(
        fallbackPoints = configuration.referralRule.referrerRewardPoints,
        fallbackLabel = configuration.referralRule.referrerRewardOutcome.label,
    ),
    referralRefereeReward = configuration.referralRule.refereeRewardOutcome.toEditorState(
        fallbackPoints = configuration.referralRule.refereeRewardPoints,
        fallbackLabel = configuration.referralRule.refereeRewardOutcome.label,
    ),
    referralCodePrefix = configuration.referralRule.referralCodePrefix,
)

fun ProgramEditorState.toDraft(
    storeId: String,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
): RewardProgramDraft {
    val configuration = toConfiguration(
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
    )
    return RewardProgramDraft(
        storeId = storeId,
        name = name.trim(),
        description = description.trim(),
        type = type,
        rulesSummary = buildRulesSummary(configuration),
        active = active,
        autoScheduleEnabled = autoScheduleEnabled,
        scheduleStartDate = scheduleStartDate.trim().takeIf { autoScheduleEnabled && it.isNotEmpty() }?.let(LocalDate::parse),
        scheduleEndDate = scheduleEndDate.trim().takeIf { autoScheduleEnabled && it.isNotEmpty() }?.let(LocalDate::parse),
        annualRepeatEnabled = annualRepeatEnabled,
        configuration = configuration,
    )
}

fun ProgramEditorState.validate(): Map<String, Int> {
    val errors = linkedMapOf<String, Int>()
    if (name.isBlank()) errors[PROGRAM_FIELD_NAME] = R.string.merchant_program_error_name_required
    if (description.isBlank()) errors[PROGRAM_FIELD_DESCRIPTION] = R.string.merchant_program_error_description_required
    if (autoScheduleEnabled) {
        val startDate = scheduleStartDate.trim().toLocalDateOrNull()
        val endDate = scheduleEndDate.trim().toLocalDateOrNull()
        if (scheduleStartDate.trim().isEmpty() || startDate == null) {
            errors[PROGRAM_FIELD_SCHEDULE_START] = R.string.merchant_program_error_schedule_start_required
        }
        if (scheduleEndDate.trim().isEmpty() || endDate == null) {
            errors[PROGRAM_FIELD_SCHEDULE_END] = R.string.merchant_program_error_schedule_end_required
        } else if (startDate != null && endDate.isBefore(startDate)) {
            errors[PROGRAM_FIELD_SCHEDULE_END] = R.string.merchant_program_error_schedule_end_before_start
        }
    }
    when (type) {
        LoyaltyProgramType.POINTS -> {
            if (positiveInt(pointsSpendStepAmount) == null) errors[PROGRAM_FIELD_POINTS_STEP] = R.string.merchant_program_error_positive_required
            if (positiveInt(pointsAwardedPerStep) == null) errors[PROGRAM_FIELD_POINTS_AWARDED] = R.string.merchant_program_error_positive_required
            if (positiveInt(pointsMinimumRedeem) == null) errors[PROGRAM_FIELD_POINTS_REDEEM] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.DIGITAL_STAMP -> {
            if (positiveInt(checkInVisitsRequired) == null) errors[PROGRAM_FIELD_CHECKIN_VISITS] = R.string.merchant_program_error_positive_required
            validateRewardOutcome(checkInReward, PROGRAM_FIELD_CHECKIN_REWARD, errors)
        }
        LoyaltyProgramType.TIER -> {
            if (tierLevels.size < 2) {
                errors[PROGRAM_FIELD_TIER_SILVER] = R.string.merchant_program_error_tier_minimum_levels
            }
            var previousThreshold = Int.MIN_VALUE
            tierLevels.forEachIndexed { index, level ->
                val name = level.name.trim()
                val threshold = nonNegativeInt(level.threshold)
                val bonusPercent = nonNegativeInt(level.bonusPercent)
                val key = tierLevelFieldKey(level.id, index)
                if (name.isBlank()) {
                    errors[key] = R.string.merchant_program_error_tier_name_required
                }
                if (threshold == null) {
                    errors[key] = R.string.merchant_program_error_tier_bonus_required
                } else if (threshold <= previousThreshold) {
                    errors[key] = R.string.merchant_program_error_tier_order
                } else {
                    previousThreshold = threshold
                }
                if (bonusPercent == null) {
                    if (level.bonusPercent.isNotBlank()) {
                        errors[key] = R.string.merchant_program_error_tier_bonus_required
                    }
                }
                if (level.rewardOutcome.isConfigured()) {
                    validateRewardOutcome(level.rewardOutcome, key, errors)
                }
            }
        }
        LoyaltyProgramType.COUPON -> {
            if (couponName.isBlank()) errors[PROGRAM_FIELD_COUPON_NAME] = R.string.merchant_program_error_coupon_name_required
            if (positiveInt(couponPointsCost) == null) errors[PROGRAM_FIELD_COUPON_POINTS] = R.string.merchant_program_error_positive_required
            if (positiveDecimal(couponDiscountAmount) == null) errors[PROGRAM_FIELD_COUPON_DISCOUNT] = R.string.merchant_program_error_positive_required
        }
        LoyaltyProgramType.PURCHASE_FREQUENCY -> {
            if (positiveInt(purchaseFrequencyCount) == null) errors[PROGRAM_FIELD_FREQUENCY_COUNT] = R.string.merchant_program_error_positive_required
            if (positiveInt(purchaseFrequencyWindowDays) == null) errors[PROGRAM_FIELD_FREQUENCY_WINDOW] = R.string.merchant_program_error_positive_required
            validateRewardOutcome(purchaseFrequencyReward, PROGRAM_FIELD_FREQUENCY_REWARD, errors)
        }
        LoyaltyProgramType.REFERRAL -> {
            validateRewardOutcome(referralReferrerReward, PROGRAM_FIELD_REFERRAL_REFERRER, errors)
            validateRewardOutcome(referralRefereeReward, PROGRAM_FIELD_REFERRAL_REFEREE, errors)
            if (referralCodePrefix.trim().length < 2) errors[PROGRAM_FIELD_REFERRAL_PREFIX] = R.string.merchant_program_error_referral_prefix
        }
        LoyaltyProgramType.HYBRID -> {
            if (positiveInt(pointsSpendStepAmount) == null) errors[PROGRAM_FIELD_POINTS_STEP] = R.string.merchant_program_error_positive_required
            if (positiveInt(checkInVisitsRequired) == null) errors[PROGRAM_FIELD_CHECKIN_VISITS] = R.string.merchant_program_error_positive_required
            validateRewardOutcome(checkInReward, PROGRAM_FIELD_CHECKIN_REWARD, errors)
            validateRewardOutcome(referralReferrerReward, PROGRAM_FIELD_REFERRAL_REFERRER, errors)
            validateRewardOutcome(referralRefereeReward, PROGRAM_FIELD_REFERRAL_REFEREE, errors)
        }
    }
    return errors
}

private fun validateRewardOutcome(
    reward: ProgramRewardOutcomeEditorState,
    fieldKey: String,
    errors: MutableMap<String, Int>,
) {
    when {
        reward.type == ProgramRewardOutcomeType.POINTS -> {
            if (positiveInt(reward.pointsAmount) == null) {
                errors[fieldKey] = R.string.merchant_program_error_positive_required
            }
        }
        reward.type.usesRewardItem() -> {
            if (reward.rewardId.isNullOrBlank()) {
                errors[fieldKey] = R.string.merchant_program_error_reward_selection_required
            }
        }
        reward.type.usesProgramBenefit() -> {
            if (reward.programId.isNullOrBlank()) {
                errors[fieldKey] = R.string.merchant_program_error_program_selection_required
            }
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(trim()) }.getOrNull()

fun ProgramEditorState.toConfiguration(
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
): RewardProgramConfiguration {
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
        levels = tierLevels.toDomainLevels(
            fallback = baseConfiguration.tierRule.sortedLevels,
            availablePrograms = availablePrograms,
            availableRewards = availableRewards,
        ),
    )
    val couponRule = CouponProgramRule(
        couponName = couponName.trim().ifBlank { baseConfiguration.couponRule.couponName },
        pointsCost = positiveInt(couponPointsCost) ?: baseConfiguration.couponRule.pointsCost,
        discountAmount = positiveDecimal(couponDiscountAmount) ?: baseConfiguration.couponRule.discountAmount,
        minimumSpendAmount = nonNegativeDecimal(couponMinimumSpendAmount) ?: baseConfiguration.couponRule.minimumSpendAmount,
    )
    val checkInRewardOutcome = checkInReward.toDomainOutcome(
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
        fallback = baseConfiguration.checkInRule.rewardOutcome,
    )
    val checkInRule = CheckInProgramRule(
        visitsRequired = positiveInt(checkInVisitsRequired) ?: baseConfiguration.checkInRule.visitsRequired,
        rewardPoints = checkInRewardOutcome.pointsAmount.takeIf { checkInRewardOutcome.type == ProgramRewardOutcomeType.POINTS } ?: 0,
        rewardName = checkInRewardOutcome.label.ifBlank {
            when {
                checkInRewardOutcome.type == ProgramRewardOutcomeType.POINTS -> baseConfiguration.checkInRule.rewardName
                checkInRewardOutcome.type.usesRewardItem() -> checkInRewardOutcome.rewardName
                checkInRewardOutcome.type.usesProgramBenefit() -> checkInRewardOutcome.programName
                else -> baseConfiguration.checkInRule.rewardName
            }
        },
        rewardOutcome = checkInRewardOutcome,
    )
    val purchaseFrequencyRewardOutcome = purchaseFrequencyReward.toDomainOutcome(
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
        fallback = baseConfiguration.purchaseFrequencyRule.rewardOutcome,
    )
    val purchaseFrequencyRule = PurchaseFrequencyProgramRule(
        purchaseCount = positiveInt(purchaseFrequencyCount) ?: baseConfiguration.purchaseFrequencyRule.purchaseCount,
        windowDays = positiveInt(purchaseFrequencyWindowDays) ?: baseConfiguration.purchaseFrequencyRule.windowDays,
        rewardPoints = purchaseFrequencyRewardOutcome.pointsAmount.takeIf { purchaseFrequencyRewardOutcome.type == ProgramRewardOutcomeType.POINTS } ?: 0,
        rewardName = purchaseFrequencyRewardOutcome.label.ifBlank {
            when {
                purchaseFrequencyRewardOutcome.type == ProgramRewardOutcomeType.POINTS -> baseConfiguration.purchaseFrequencyRule.rewardName
                purchaseFrequencyRewardOutcome.type.usesRewardItem() -> purchaseFrequencyRewardOutcome.rewardName
                purchaseFrequencyRewardOutcome.type.usesProgramBenefit() -> purchaseFrequencyRewardOutcome.programName
                else -> baseConfiguration.purchaseFrequencyRule.rewardName
            }
        },
        rewardOutcome = purchaseFrequencyRewardOutcome,
    )
    val referrerRewardOutcome = referralReferrerReward.toDomainOutcome(
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
        fallback = baseConfiguration.referralRule.referrerRewardOutcome,
    )
    val refereeRewardOutcome = referralRefereeReward.toDomainOutcome(
        availablePrograms = availablePrograms,
        availableRewards = availableRewards,
        fallback = baseConfiguration.referralRule.refereeRewardOutcome,
    )
    val referralRule = ReferralProgramRule(
        referrerRewardPoints = referrerRewardOutcome.pointsAmount.takeIf { referrerRewardOutcome.type == ProgramRewardOutcomeType.POINTS } ?: 0,
        refereeRewardPoints = refereeRewardOutcome.pointsAmount.takeIf { refereeRewardOutcome.type == ProgramRewardOutcomeType.POINTS } ?: 0,
        referralCodePrefix = referralCodePrefix.trim().uppercase().ifBlank { baseConfiguration.referralRule.referralCodePrefix },
        referrerRewardOutcome = referrerRewardOutcome,
        refereeRewardOutcome = refereeRewardOutcome,
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
    configuration.referralEnabled -> "${configuration.referralRule.referrerRewardOutcome.summaryText()} / ${configuration.referralRule.refereeRewardOutcome.summaryText()}"
    configuration.purchaseFrequencyEnabled -> "${configuration.purchaseFrequencyRule.purchaseCount} purchases in ${configuration.purchaseFrequencyRule.windowDays} days"
    configuration.couponEnabled -> "${configuration.couponRule.couponName} for ${configuration.couponRule.pointsCost} pts"
    configuration.tierTrackingEnabled -> configuration.tierRule.configurableLevels.joinToString(", ") { "${it.name} ${it.threshold}" }
    configuration.visitCheckInEnabled -> "Reward after ${configuration.checkInRule.visitsRequired} check-ins"
    configuration.cashbackEnabled -> "${decimalString(configuration.cashbackRule.cashbackPercent)}% cashback"
    else -> "${configuration.pointsRule.pointsAwardedPerStep} point per ${configuration.pointsRule.spendStepAmount} spent"
}

internal fun ProgramRewardOutcome.summaryText(): String = when (type) {
    ProgramRewardOutcomeType.POINTS -> "${pointsAmount.coerceAtLeast(0)} pts"
    else -> when {
        type.usesRewardItem() -> rewardName.ifBlank { label }
        type.usesProgramBenefit() -> programName.ifBlank { label }
        else -> label
    }
}

private fun ProgramRewardOutcome.toEditorState(
    fallbackPoints: Int,
    fallbackLabel: String,
): ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(
    type = type,
    label = label.ifBlank { fallbackLabel },
    pointsAmount = (pointsAmount.takeIf { it > 0 } ?: fallbackPoints).takeIf { it > 0 }?.toString().orEmpty(),
    rewardId = rewardId,
    programId = programId,
)

private fun ProgramRewardOutcomeEditorState.toDomainOutcome(
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
    fallback: ProgramRewardOutcome,
): ProgramRewardOutcome {
    val selectedReward = availableRewards.firstOrNull { it.id == rewardId }
    val selectedProgram = availablePrograms.firstOrNull { it.id == programId }
    return when {
        type == ProgramRewardOutcomeType.POINTS -> ProgramRewardOutcome(
            type = ProgramRewardOutcomeType.POINTS,
            label = label.trim().ifBlank { fallback.label },
            pointsAmount = positiveInt(pointsAmount) ?: fallback.pointsAmount,
        )
        type.usesRewardItem() -> ProgramRewardOutcome(
            type = selectedReward?.rewardType?.toOutcomeType() ?: fallback.type,
            label = selectedReward?.name.orEmpty(),
            rewardId = selectedReward?.id ?: fallback.rewardId,
            rewardName = selectedReward?.name ?: fallback.rewardName,
            rewardType = selectedReward?.rewardType ?: fallback.rewardType,
        )
        type.usesProgramBenefit() -> ProgramRewardOutcome(
            type = selectedProgram?.type?.toOutcomeType() ?: fallback.type,
            label = selectedProgram?.name.orEmpty(),
            programId = selectedProgram?.id ?: fallback.programId,
            programName = selectedProgram?.name ?: fallback.programName,
        )
        else -> fallback
    }
}

private fun positiveInt(value: String): Int? = value.trim().toIntOrNull()?.takeIf { it > 0 }
private fun nonNegativeInt(value: String): Int? = value.trim().toIntOrNull()?.takeIf { it >= 0 }
private fun positiveDecimal(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it > 0.0 }
private fun nonNegativeDecimal(value: String): Double? = value.trim().toDoubleOrNull()?.takeIf { it >= 0.0 }

private fun decimalString(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

private fun List<TierLevelRule>.toEditorState(): List<TierLevelEditorState> =
    map { level ->
        TierLevelEditorState(
            id = level.id,
            name = level.name,
            threshold = level.threshold.toString(),
            bonusPercent = level.bonusPercent.coerceAtLeast(0).toString(),
            rewardOutcome = level.rewardOutcome.toEditorState(
                fallbackPoints = level.rewardOutcome.pointsAmount,
                fallbackLabel = level.rewardOutcome.label,
            ),
        )
    }

private fun List<TierLevelEditorState>.toDomainLevels(
    fallback: List<TierLevelRule>,
    availablePrograms: List<RewardProgram>,
    availableRewards: List<Reward>,
): List<TierLevelRule> {
    val fallbackById = fallback.associateBy { it.id }
    val normalized = mapIndexedNotNull { index, level ->
        val fallbackLevel = fallbackById[level.id]
        val threshold = nonNegativeInt(level.threshold) ?: fallbackLevel?.threshold ?: (index * 250)
        val bonusPercent = nonNegativeInt(level.bonusPercent) ?: 0
        val rewardOutcome = level.rewardOutcome
            .takeIf { it.isConfigured() }
            ?.toDomainOutcome(
                availablePrograms = availablePrograms,
                availableRewards = availableRewards,
                fallback = fallbackLevel?.rewardOutcome ?: ProgramRewardOutcome(),
            )
            ?: ProgramRewardOutcome()
        level.name.trim()
            .ifBlank { fallbackLevel?.name }
            ?.let { name ->
                TierLevelRule(
                    id = level.id,
                    name = name,
                    threshold = threshold,
                    bonusPercent = bonusPercent,
                    rewardOutcome = rewardOutcome,
                )
            }
    }
    return if (normalized.isEmpty()) fallback else normalized
}

internal fun tierLevelFieldKey(levelId: String, index: Int): String = "tier_level_${index}_$levelId"

private fun RewardType.toOutcomeType(): ProgramRewardOutcomeType = when (this) {
    RewardType.FREE_PRODUCT -> ProgramRewardOutcomeType.FREE_PRODUCT
    RewardType.DISCOUNT_COUPON -> ProgramRewardOutcomeType.DISCOUNT_COUPON
    RewardType.GIFT_ITEM -> ProgramRewardOutcomeType.GIFT_ITEM
    RewardType.SPECIAL_PROMOTION -> ProgramRewardOutcomeType.SPECIAL_PROMOTION
}

private fun LoyaltyProgramType.toOutcomeType(): ProgramRewardOutcomeType = when (this) {
    LoyaltyProgramType.POINTS -> ProgramRewardOutcomeType.PROGRAM_POINTS
    LoyaltyProgramType.DIGITAL_STAMP -> ProgramRewardOutcomeType.PROGRAM_DIGITAL_STAMP
    LoyaltyProgramType.TIER -> ProgramRewardOutcomeType.PROGRAM_TIER
    LoyaltyProgramType.COUPON -> ProgramRewardOutcomeType.PROGRAM_COUPON
    LoyaltyProgramType.PURCHASE_FREQUENCY -> ProgramRewardOutcomeType.PROGRAM_PURCHASE_FREQUENCY
    LoyaltyProgramType.REFERRAL -> ProgramRewardOutcomeType.PROGRAM_REFERRAL
    LoyaltyProgramType.HYBRID -> ProgramRewardOutcomeType.PROGRAM_HYBRID
}
