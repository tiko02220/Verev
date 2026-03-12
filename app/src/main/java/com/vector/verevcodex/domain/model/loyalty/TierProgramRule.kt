package com.vector.verevcodex.domain.model.loyalty

data class TierProgramRule(
    val silverThreshold: Int = 250,
    val goldThreshold: Int = 500,
    val vipThreshold: Int = 1000,
    val tierBonusPercent: Int = 10,
)
