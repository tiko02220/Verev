package com.vector.verevcodex.domain.model.loyalty

data class CashbackProgramRule(
    val cashbackPercent: Double = 5.0,
    val minimumSpendAmount: Double = 0.0,
)
