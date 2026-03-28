package com.vector.verevcodex.domain.model.loyalty

data class CheckInProgramRule(
    val visitsRequired: Int = 5,
    val rewardPoints: Int = 25,
    val rewardName: String = "Check-in reward",
    val rewardOutcome: ProgramRewardOutcome = ProgramRewardOutcome(
        type = ProgramRewardOutcomeType.POINTS,
        label = "Check-in reward",
        pointsAmount = 25,
    ),
)
