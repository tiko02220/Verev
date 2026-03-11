package com.vector.verevcodex.domain.model

data class RewardProgramDraft(
    val storeId: String,
    val name: String,
    val description: String,
    val type: LoyaltyProgramType,
    val rulesSummary: String,
    val active: Boolean,
    val configuration: RewardProgramConfiguration,
)
