package com.vector.verevcodex.domain.model.billing

data class SubscriptionPlanOption(
    val id: String,
    val monthlyPrice: Double,
    val currencyCode: String,
)
