package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

data class ProgramPerformance(
    val programId: String,
    val name: String,
    val type: LoyaltyProgramType,
    val active: Boolean,
    val scanActionsEnabled: Int,
    val memberCount: Int,
    val redemptionRate: Double,
)
