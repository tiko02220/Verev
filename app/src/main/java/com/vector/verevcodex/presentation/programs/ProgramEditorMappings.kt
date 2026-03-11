package com.vector.verevcodex.presentation.programs

import com.vector.verevcodex.domain.model.LoyaltyProgramType
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.domain.model.RewardProgramConfiguration
import com.vector.verevcodex.domain.model.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.RewardProgramDraft
import com.vector.verevcodex.domain.model.RewardProgramScanAction

val ProgramEditorState.isEditing: Boolean
    get() = programId != null

fun ProgramEditorState.toDraft(storeId: String): RewardProgramDraft = RewardProgramDraft(
    storeId = storeId,
    name = name.trim(),
    description = description.trim(),
    type = type,
    rulesSummary = rulesSummary.trim(),
    active = active,
    configuration = RewardProgramConfiguration(
        earningEnabled = earningEnabled,
        rewardRedemptionEnabled = rewardRedemptionEnabled,
        visitCheckInEnabled = visitCheckInEnabled,
        cashbackEnabled = cashbackEnabled,
        tierTrackingEnabled = tierTrackingEnabled,
        scanActions = buildSet {
            if (earningEnabled) add(RewardProgramScanAction.EARN_POINTS)
            if (rewardRedemptionEnabled) add(RewardProgramScanAction.REDEEM_REWARDS)
            if (visitCheckInEnabled) add(RewardProgramScanAction.CHECK_IN)
            if (cashbackEnabled) add(RewardProgramScanAction.APPLY_CASHBACK)
            if (tierTrackingEnabled) add(RewardProgramScanAction.TRACK_TIER_PROGRESS)
        },
    ),
)

fun defaultProgramEditorState(): ProgramEditorState {
    val configuration = RewardProgramConfigurationFactory.defaultFor(LoyaltyProgramType.POINTS, active = true)
    return ProgramEditorState(
        earningEnabled = configuration.earningEnabled,
        rewardRedemptionEnabled = configuration.rewardRedemptionEnabled,
        visitCheckInEnabled = configuration.visitCheckInEnabled,
        cashbackEnabled = configuration.cashbackEnabled,
        tierTrackingEnabled = configuration.tierTrackingEnabled,
    )
}

fun RewardProgram.toEditorState(): ProgramEditorState = ProgramEditorState(
    programId = id,
    name = name,
    description = description,
    type = type,
    rulesSummary = rulesSummary,
    active = active,
    earningEnabled = configuration.earningEnabled,
    rewardRedemptionEnabled = configuration.rewardRedemptionEnabled,
    visitCheckInEnabled = configuration.visitCheckInEnabled,
    cashbackEnabled = configuration.cashbackEnabled,
    tierTrackingEnabled = configuration.tierTrackingEnabled,
)
