package com.vector.verevcodex.domain.model

data class RewardProgramConfiguration(
    val earningEnabled: Boolean,
    val rewardRedemptionEnabled: Boolean,
    val visitCheckInEnabled: Boolean,
    val cashbackEnabled: Boolean,
    val tierTrackingEnabled: Boolean,
    val scanActions: Set<RewardProgramScanAction>,
)
