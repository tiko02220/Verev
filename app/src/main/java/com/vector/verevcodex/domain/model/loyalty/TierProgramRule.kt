package com.vector.verevcodex.domain.model.loyalty

data class TierProgramRule(
    val levels: List<TierLevelRule> = defaultTierLevels(),
) {
    val sortedLevels: List<TierLevelRule> = normalizeLevels(levels)

    val configurableLevels: List<TierLevelRule>
        get() = sortedLevels

    val silverThreshold: Int
        get() = configurableLevels.getOrNull(0)?.threshold ?: 250

    val goldThreshold: Int
        get() = configurableLevels.getOrNull(1)?.threshold ?: 500

    val vipThreshold: Int
        get() = configurableLevels.getOrNull(2)?.threshold ?: 1000

    val tierBonusPercent: Int
        get() = configurableLevels.lastOrNull()?.bonusPercent ?: 10

    fun levelForTierName(name: String?): TierLevelRule? =
        name?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { tierName ->
                sortedLevels.firstOrNull { it.name.equals(tierName, ignoreCase = true) }
            }

    fun nextLevelAfter(name: String?): TierLevelRule? {
        val current = levelForTierName(name) ?: sortedLevels.firstOrNull()
        val currentIndex = sortedLevels.indexOfFirst { it.id == current?.id }
        return sortedLevels.getOrNull(currentIndex + 1)
    }

    fun currentTierFloor(name: String?): Int =
        levelForTierName(name)?.threshold ?: sortedLevels.firstOrNull()?.threshold ?: 0

    companion object {
        fun defaultTierLevels(): List<TierLevelRule> = listOf(
            TierLevelRule(id = "tier_1", name = "Tier 1", threshold = 0, bonusPercent = 0),
            TierLevelRule(id = "tier_2", name = "Tier 2", threshold = 250, bonusPercent = 5),
            TierLevelRule(id = "tier_3", name = "Tier 3", threshold = 500, bonusPercent = 10),
            TierLevelRule(id = "tier_4", name = "Tier 4", threshold = 1000, bonusPercent = 15),
        )

        private fun normalizeLevels(input: List<TierLevelRule>): List<TierLevelRule> {
            val unique = input
                .filter { it.name.isNotBlank() }
                .distinctBy { it.id }
                .sortedBy { it.threshold }
            if (unique.isEmpty()) return defaultTierLevels()
            return unique
                .mapIndexed { index, level ->
                    val minimumThreshold = if (index == 0) 0 else (unique.getOrNull(index - 1)?.threshold ?: 0) + 1
                    level.copy(
                        threshold = level.threshold.coerceAtLeast(minimumThreshold),
                        bonusPercent = level.bonusPercent.coerceAtLeast(0),
                    )
                }
        }
    }
}
