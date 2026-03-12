package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.Identifiable
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

data class RewardProgram(
    override val id: String,
    val storeId: String,
    val name: String,
    val description: String,
    val type: LoyaltyProgramType,
    val rulesSummary: String,
    val active: Boolean,
    val configuration: RewardProgramConfiguration,
) : Identifiable
