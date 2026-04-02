package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.loyalty.ProgramRewardOutcomeType
import com.vector.verevcodex.domain.model.loyalty.usesProgramBenefit

enum class ProgramBenefitChoice {
    POINTS,
    REWARD_CATALOG,
    PROGRAM,
}

fun ProgramRewardOutcomeEditorState.benefitChoice(): ProgramBenefitChoice = when {
    type == ProgramRewardOutcomeType.POINTS -> ProgramBenefitChoice.POINTS
    type.usesProgramBenefit() -> ProgramBenefitChoice.PROGRAM
    else -> ProgramBenefitChoice.REWARD_CATALOG
}
