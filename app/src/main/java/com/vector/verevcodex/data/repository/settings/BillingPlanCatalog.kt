package com.vector.verevcodex.data.repository.settings

import com.vector.verevcodex.domain.model.billing.SubscriptionPlanOption

internal object BillingPlanCatalog {
    val plans: List<SubscriptionPlanOption> = listOf(
        SubscriptionPlanOption(
            id = "starter",
            monthlyPrice = 39.0,
            currencyCode = "USD",
        ),
        SubscriptionPlanOption(
            id = "business_standard",
            monthlyPrice = 99.0,
            currencyCode = "USD",
        ),
        SubscriptionPlanOption(
            id = "growth_plus",
            monthlyPrice = 179.0,
            currencyCode = "USD",
        ),
    )

    fun find(planId: String?): SubscriptionPlanOption? = plans.firstOrNull { it.id == planId }
}
