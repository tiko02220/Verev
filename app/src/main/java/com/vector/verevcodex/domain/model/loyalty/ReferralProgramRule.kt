package com.vector.verevcodex.domain.model.loyalty

data class ReferralProgramRule(
    val referrerRewardPoints: Int = 50,
    val refereeRewardPoints: Int = 25,
    val referralCodePrefix: String = "REF",
)
