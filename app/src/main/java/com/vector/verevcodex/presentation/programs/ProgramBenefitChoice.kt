package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType

enum class ProgramBenefitChoice {
    POINTS,
    REWARD_CATALOG,
}

fun ProgramRewardOutcomeEditorState.benefitChoice(): ProgramBenefitChoice = when {
    type == ProgramRewardOutcomeType.POINTS -> ProgramBenefitChoice.POINTS
    else -> ProgramBenefitChoice.REWARD_CATALOG
}
