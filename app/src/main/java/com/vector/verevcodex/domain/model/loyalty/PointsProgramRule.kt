package com.vector.verevcodex.domain.model.loyalty

data class PointsProgramRule(
    val spendStepAmount: Int = 100,
    val pointsAwardedPerStep: Int = 1,
    val welcomeBonusPoints: Int = 0,
    val minimumRedeemPoints: Int = 50,
)
