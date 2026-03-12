package com.vector.verevcodex.presentation.transactions

data class CheckoutTotals(
    val subtotal: Double = 0.0,
    val promotionDiscount: Double = 0.0,
    val promotionBonusPoints: Int = 0,
    val redeemablePoints: Int = 0,
    val redeemedPoints: Int = 0,
    val finalAmount: Double = 0.0,
    val pointsEarned: Int = 0,
)
