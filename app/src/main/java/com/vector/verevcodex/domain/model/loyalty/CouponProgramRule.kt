package com.vector.verevcodex.domain.model.loyalty

data class CouponProgramRule(
    val couponName: String = "Reward Coupon",
    val pointsCost: Int = 100,
    val discountAmount: Double = 1000.0,
    val minimumSpendAmount: Double = 5000.0,
)
