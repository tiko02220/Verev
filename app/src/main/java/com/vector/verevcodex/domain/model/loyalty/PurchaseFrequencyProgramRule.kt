package com.vector.verevcodex.domain.model.loyalty

data class PurchaseFrequencyProgramRule(
    val purchaseCount: Int = 5,
    val windowDays: Int = 30,
    val rewardPoints: Int = 50,
    val rewardName: String = "Repeat purchase reward",
    val rewardOutcome: ProgramRewardOutcome = ProgramRewardOutcome(
        type = ProgramRewardOutcomeType.POINTS,
        label = "Repeat purchase reward",
        pointsAmount = 50,
    ),
)
