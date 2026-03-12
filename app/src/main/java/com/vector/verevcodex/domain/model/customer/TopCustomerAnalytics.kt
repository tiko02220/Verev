package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.common.LoyaltyTier

data class TopCustomerAnalytics(
    val customerId: String,
    val customerName: String,
    val totalSpent: Double,
    val totalVisits: Int,
    val loyaltyTier: LoyaltyTier,
)
