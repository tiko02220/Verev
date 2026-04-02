package com.vector.verevcodex.domain.model.loyalty

data class TierProgramRule(
    val levels: List<TierLevelRule> = defaultTierLevels(),
) {
    val sortedLevels: List<TierLevelRule> = normalizeLevels(levels)

    val configurableLevels: List<TierLevelRule>
        get() = sortedLevels

    val thresholdBasis: TierThresholdBasis
        get() = configurableLevels.firstOrNull()?.thresholdBasis ?: TierThresholdBasis.POINTS

    val silverThreshold: Int
        get() = configurableLevels.getOrNull(1)?.threshold ?: 250

    val goldThreshold: Int
        get() = configurableLevels.getOrNull(2)?.threshold ?: 500

    val vipThreshold: Int
        get() = configurableLevels.lastOrNull()?.threshold ?: 1000

    val tierBonusPercent: Int
        get() = configurableLevels
            .lastOrNull { it.benefitType == TierBenefitType.BONUS_PERCENT }
            ?.bonusPercent
            ?: 10

    fun metricValue(points: Int, totalSpent: Double): Double = when (thresholdBasis) {
        TierThresholdBasis.POINTS -> points.toDouble()
        TierThresholdBasis.SPEND -> totalSpent
    }

    fun levelForTierName(name: String?): TierLevelRule? =
        name?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { tierName ->
                sortedLevels.firstOrNull { it.name.equals(tierName, ignoreCase = true) }
            }

    fun earnedLevelFor(points: Int, totalSpent: Double): TierLevelRule? {
        val metric = metricValue(points = points, totalSpent = totalSpent)
        return configurableLevels
            .sortedBy { it.threshold }
            .lastOrNull { metric >= it.threshold.toDouble() }
    }

    fun nextLevelAfter(name: String?): TierLevelRule? {
        val current = levelForTierName(name) ?: sortedLevels.firstOrNull()
        val currentIndex = sortedLevels.indexOfFirst { it.id == current?.id }
        return sortedLevels.getOrNull(currentIndex + 1)
    }

    fun nextLevelAfter(points: Int, totalSpent: Double): TierLevelRule? {
        val current = earnedLevelFor(points = points, totalSpent = totalSpent) ?: sortedLevels.firstOrNull()
        val currentIndex = sortedLevels.indexOfFirst { it.id == current?.id }
        return sortedLevels.getOrNull(currentIndex + 1)
    }

    fun currentTierFloor(name: String?): Int =
        levelForTierName(name)?.threshold ?: sortedLevels.firstOrNull()?.threshold ?: 0

    fun currentTierFloor(points: Int, totalSpent: Double): Int =
        earnedLevelFor(points = points, totalSpent = totalSpent)?.threshold
            ?: sortedLevels.firstOrNull()?.threshold
            ?: 0

    fun activeDiscountPercent(points: Int, totalSpent: Double): Int =
        earnedLevelFor(points = points, totalSpent = totalSpent)
            ?.takeIf { it.benefitType == TierBenefitType.DISCOUNT_PERCENT }
            ?.bonusPercent
            ?.coerceAtLeast(0)
            ?: 0

    fun activeBonusPercent(points: Int, totalSpent: Double): Int =
        earnedLevelFor(points = points, totalSpent = totalSpent)
            ?.takeIf { it.benefitType == TierBenefitType.BONUS_PERCENT }
            ?.bonusPercent
            ?.coerceAtLeast(0)
            ?: 0

    companion object {
        fun defaultTierLevels(
            thresholdBasis: TierThresholdBasis = TierThresholdBasis.POINTS,
        ): List<TierLevelRule> = listOf(
            TierLevelRule(id = "tier_1", name = "Bronze", threshold = 0, thresholdBasis = thresholdBasis, benefitType = TierBenefitType.BONUS_PERCENT, bonusPercent = 0),
            TierLevelRule(id = "tier_2", name = "Silver", threshold = 250, thresholdBasis = thresholdBasis, benefitType = TierBenefitType.BONUS_PERCENT, bonusPercent = 5),
            TierLevelRule(id = "tier_3", name = "Gold", threshold = 500, thresholdBasis = thresholdBasis, benefitType = TierBenefitType.BONUS_PERCENT, bonusPercent = 10),
            TierLevelRule(id = "tier_4", name = "Platinum", threshold = 750, thresholdBasis = thresholdBasis, benefitType = TierBenefitType.BONUS_PERCENT, bonusPercent = 15),
            TierLevelRule(id = "tier_5", name = "VIP", threshold = 1000, thresholdBasis = thresholdBasis, benefitType = TierBenefitType.BONUS_PERCENT, bonusPercent = 20),
        )

        private fun normalizeLevels(input: List<TierLevelRule>): List<TierLevelRule> {
            val unique = input
                .filter { it.name.isNotBlank() }
                .distinctBy { it.id }
                .sortedBy { it.threshold }
            if (unique.isEmpty()) return defaultTierLevels()
            val normalizedBasis = unique.firstOrNull()?.thresholdBasis ?: TierThresholdBasis.POINTS
            return unique
                .mapIndexed { index, level ->
                    val minimumThreshold = if (index == 0) 0 else (unique.getOrNull(index - 1)?.threshold ?: 0) + 1
                    level.copy(
                        threshold = level.threshold.coerceAtLeast(minimumThreshold),
                        thresholdBasis = normalizedBasis,
                        bonusPercent = level.bonusPercent.coerceAtLeast(0),
                    )
                }
        }
    }
}
