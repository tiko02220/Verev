package com.vector.verevcodex.domain.model.loyalty

data class TierLevelRule(
    val id: String,
    val name: String,
    val threshold: Int,
    val thresholdBasis: TierThresholdBasis = TierThresholdBasis.POINTS,
    val benefitType: TierBenefitType = TierBenefitType.BONUS_PERCENT,
    val bonusPercent: Int,
    val rewardOutcome: ProgramRewardOutcome = ProgramRewardOutcome(),
)
