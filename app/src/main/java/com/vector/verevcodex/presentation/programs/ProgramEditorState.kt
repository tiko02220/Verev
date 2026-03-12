package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

data class ProgramEditorState(
    val programId: String? = null,
    val name: String = "",
    val description: String = "",
    val type: LoyaltyProgramType = LoyaltyProgramType.POINTS,
    val active: Boolean = true,
    val pointsSpendStepAmount: String = "100",
    val pointsAwardedPerStep: String = "1",
    val pointsWelcomeBonus: String = "0",
    val pointsMinimumRedeem: String = "50",
    val cashbackPercent: String = "5",
    val cashbackMinimumSpendAmount: String = "0",
    val tierSilverThreshold: String = "250",
    val tierGoldThreshold: String = "500",
    val tierVipThreshold: String = "1000",
    val tierBonusPercent: String = "10",
    val couponName: String = "Reward Coupon",
    val couponPointsCost: String = "100",
    val couponDiscountAmount: String = "1000",
    val couponMinimumSpendAmount: String = "5000",
    val checkInVisitsRequired: String = "5",
    val checkInRewardPoints: String = "25",
    val checkInRewardName: String = "Check-in reward",
    val purchaseFrequencyCount: String = "5",
    val purchaseFrequencyWindowDays: String = "30",
    val purchaseFrequencyRewardPoints: String = "50",
    val purchaseFrequencyRewardName: String = "Repeat purchase reward",
    val referralReferrerRewardPoints: String = "50",
    val referralRefereeRewardPoints: String = "25",
    val referralCodePrefix: String = "REF",
)

val ProgramEditorState.isEditing: Boolean
    get() = programId != null
