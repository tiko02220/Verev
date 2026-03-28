package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

internal val DefaultTierNames = listOf("Bronze", "Silver", "Gold", "Platinum", "VIP")

data class TierLevelEditorState(
    val id: String,
    val name: String,
    val threshold: String,
    val bonusPercent: String,
    val rewardOutcome: ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(),
)

data class ProgramEditorState(
    val programId: String? = null,
    val name: String = "",
    val description: String = "",
    val type: LoyaltyProgramType = LoyaltyProgramType.POINTS,
    val active: Boolean = true,
    val autoScheduleEnabled: Boolean = false,
    val scheduleStartDate: String = "",
    val scheduleEndDate: String = "",
    val annualRepeatEnabled: Boolean = false,
    val pointsSpendStepAmount: String = "100",
    val pointsAwardedPerStep: String = "1",
    val pointsWelcomeBonus: String = "",
    val pointsMinimumRedeem: String = "50",
    val cashbackPercent: String = "5",
    val cashbackMinimumSpendAmount: String = "0",
    val tierLevels: List<TierLevelEditorState> = listOf(
        TierLevelEditorState(
            id = "tier_1",
            name = DefaultTierNames[0],
            threshold = "0",
            bonusPercent = "0",
            rewardOutcome = ProgramRewardOutcomeEditorState(),
        ),
        TierLevelEditorState(
            id = "tier_2",
            name = DefaultTierNames[1],
            threshold = "250",
            bonusPercent = "0",
            rewardOutcome = ProgramRewardOutcomeEditorState(),
        ),
        TierLevelEditorState(
            id = "tier_3",
            name = DefaultTierNames[2],
            threshold = "500",
            bonusPercent = "0",
            rewardOutcome = ProgramRewardOutcomeEditorState(),
        ),
        TierLevelEditorState(
            id = "tier_4",
            name = DefaultTierNames[3],
            threshold = "750",
            bonusPercent = "0",
            rewardOutcome = ProgramRewardOutcomeEditorState(),
        ),
        TierLevelEditorState(
            id = "tier_5",
            name = DefaultTierNames[4],
            threshold = "1000",
            bonusPercent = "0",
            rewardOutcome = ProgramRewardOutcomeEditorState(),
        ),
    ),
    val couponName: String = "",
    val couponPointsCost: String = "100",
    val couponDiscountAmount: String = "1000",
    val couponMinimumSpendAmount: String = "",
    val checkInVisitsRequired: String = "5",
    val checkInReward: ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(),
    val purchaseFrequencyCount: String = "5",
    val purchaseFrequencyWindowDays: String = "30",
    val purchaseFrequencyReward: ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(),
    val referralReferrerReward: ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(),
    val referralRefereeReward: ProgramRewardOutcomeEditorState = ProgramRewardOutcomeEditorState(),
    val referralCodePrefix: String = "",
)

val ProgramEditorState.isEditing: Boolean
    get() = programId != null

val ProgramEditorState.checkInRewardPoints: String
    get() = checkInReward.pointsAmount

val ProgramEditorState.checkInRewardName: String
    get() = checkInReward.label

val ProgramEditorState.purchaseFrequencyRewardPoints: String
    get() = purchaseFrequencyReward.pointsAmount

val ProgramEditorState.purchaseFrequencyRewardName: String
    get() = purchaseFrequencyReward.label

val ProgramEditorState.referralReferrerRewardPoints: String
    get() = referralReferrerReward.pointsAmount

val ProgramEditorState.referralRefereeRewardPoints: String
    get() = referralRefereeReward.pointsAmount
