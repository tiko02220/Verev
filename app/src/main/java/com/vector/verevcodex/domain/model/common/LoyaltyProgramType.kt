package com.vector.verevcodex.domain.model.common

enum class LoyaltyProgramType {
    POINTS,
    DIGITAL_STAMP,
    TIER,
    PURCHASE_FREQUENCY,
    REFERRAL,
}

fun LoyaltyProgramType.supportsOneTimePerCustomer(): Boolean = when (this) {
    LoyaltyProgramType.DIGITAL_STAMP,
    LoyaltyProgramType.PURCHASE_FREQUENCY,
        -> true
    LoyaltyProgramType.POINTS,
    LoyaltyProgramType.TIER,
    LoyaltyProgramType.REFERRAL,
        -> false
}
