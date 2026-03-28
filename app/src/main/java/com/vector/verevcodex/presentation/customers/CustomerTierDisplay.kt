package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.loyalty.TierLevelRule
import com.vector.verevcodex.domain.model.loyalty.TierProgramRule

internal fun Customer.resolveDisplayedTier(tierRule: TierProgramRule?): Customer {
    val earnedLevel = tierRule?.earnedLevelForPoints(currentPoints)
    return if (earnedLevel == null) {
        copy(loyaltyTierLabel = "")
    } else {
        copy(
            loyaltyTier = tierRule.loyaltyTierForLevel(earnedLevel),
            loyaltyTierLabel = earnedLevel.name,
        )
    }
}

internal fun Customer.hasDisplayedTier(tierRule: TierProgramRule?): Boolean =
    tierRule?.earnedLevelForPoints(currentPoints) != null

private fun TierProgramRule.earnedLevelForPoints(points: Int): TierLevelRule? =
    configurableLevels
        .sortedBy { it.threshold }
        .lastOrNull { points >= it.threshold }

private fun TierProgramRule.loyaltyTierForLevel(level: TierLevelRule): LoyaltyTier {
    val index = configurableLevels.indexOfFirst { it.id == level.id }.coerceAtLeast(0)
    return when (index) {
        0 -> LoyaltyTier.BRONZE
        1 -> LoyaltyTier.SILVER
        2 -> LoyaltyTier.GOLD
        else -> LoyaltyTier.VIP
    }
}
