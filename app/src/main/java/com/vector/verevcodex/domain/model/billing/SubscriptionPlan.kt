package com.vector.verevcodex.domain.model.billing

import java.time.LocalDate

data class SubscriptionPlan(
    val id: String,
    val ownerId: String,
    val name: String,
    val monthlyPrice: Double,
    val currencyCode: String,
    val renewalDate: LocalDate,
    val active: Boolean,
)
