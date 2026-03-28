package com.vector.verevcodex.domain.model.loyalty

data class ReferralProgramRule(
    val referrerRewardPoints: Int = 50,
    val refereeRewardPoints: Int = 25,
    val referralCodePrefix: String = "REF",
    val referrerRewardOutcome: ProgramRewardOutcome = ProgramRewardOutcome(
        type = ProgramRewardOutcomeType.POINTS,
        label = "Referrer reward",
        pointsAmount = 50,
    ),
    val refereeRewardOutcome: ProgramRewardOutcome = ProgramRewardOutcome(
        type = ProgramRewardOutcomeType.POINTS,
        label = "Friend reward",
        pointsAmount = 25,
    ),
)
