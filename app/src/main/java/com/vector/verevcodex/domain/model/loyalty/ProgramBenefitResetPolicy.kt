package com.vector.verevcodex.domain.model.loyalty

enum class ProgramBenefitResetType {
    NEVER,
    MONTHLY,
    YEARLY,
    CUSTOM,
}

data class ProgramBenefitResetPolicy(
    val type: ProgramBenefitResetType = ProgramBenefitResetType.NEVER,
    val customDays: Int? = null,
)
