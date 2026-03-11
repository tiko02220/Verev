package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.LoyaltyProgramType

data class ProgramEditorState(
    val programId: String? = null,
    val name: String = "",
    val description: String = "",
    val type: LoyaltyProgramType = LoyaltyProgramType.POINTS,
    val rulesSummary: String = "",
    val active: Boolean = true,
    val earningEnabled: Boolean = true,
    val rewardRedemptionEnabled: Boolean = true,
    val visitCheckInEnabled: Boolean = false,
    val cashbackEnabled: Boolean = false,
    val tierTrackingEnabled: Boolean = false,
)
