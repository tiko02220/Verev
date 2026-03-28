package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.usesProgramBenefit
import com.vector.verevcodex.domain.model.loyalty.usesRewardItem

data class ProgramRewardOutcomeEditorState(
    val type: ProgramRewardOutcomeType = ProgramRewardOutcomeType.POINTS,
    val label: String = "",
    val pointsAmount: String = "",
    val rewardId: String? = null,
    val programId: String? = null,
)

fun ProgramRewardOutcomeEditorState.isConfigured(): Boolean = when {
    type == ProgramRewardOutcomeType.POINTS -> pointsAmount.trim().toIntOrNull()?.let { it > 0 } == true
    type.usesRewardItem() -> !rewardId.isNullOrBlank()
    type.usesProgramBenefit() -> !programId.isNullOrBlank()
    else -> false
}

enum class ProgramRewardSlot {
    CHECK_IN,
    PURCHASE_FREQUENCY,
    REFERRAL_REFERRER,
    REFERRAL_REFEREE,
}
