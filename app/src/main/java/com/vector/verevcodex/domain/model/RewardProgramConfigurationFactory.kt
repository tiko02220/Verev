package com.vector.verevcodex.domain.model

object RewardProgramConfigurationFactory {
    fun defaultFor(
        type: LoyaltyProgramType,
        active: Boolean,
    ): RewardProgramConfiguration {
        if (!active) {
            return RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                scanActions = emptySet(),
            )
        }

        return when (type) {
            LoyaltyProgramType.POINTS -> RewardProgramConfiguration(
                earningEnabled = true,
                rewardRedemptionEnabled = true,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                scanActions = setOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.REDEEM_REWARDS),
            )
            LoyaltyProgramType.CASHBACK -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = false,
                cashbackEnabled = true,
                tierTrackingEnabled = false,
                scanActions = setOf(RewardProgramScanAction.APPLY_CASHBACK),
            )
            LoyaltyProgramType.DIGITAL_STAMP -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = true,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                scanActions = setOf(RewardProgramScanAction.CHECK_IN),
            )
            LoyaltyProgramType.TIER -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = true,
                cashbackEnabled = false,
                tierTrackingEnabled = true,
                scanActions = setOf(RewardProgramScanAction.CHECK_IN, RewardProgramScanAction.TRACK_TIER_PROGRESS),
            )
            LoyaltyProgramType.HYBRID -> RewardProgramConfiguration(
                earningEnabled = true,
                rewardRedemptionEnabled = true,
                visitCheckInEnabled = true,
                cashbackEnabled = true,
                tierTrackingEnabled = true,
                scanActions = setOf(
                    RewardProgramScanAction.EARN_POINTS,
                    RewardProgramScanAction.REDEEM_REWARDS,
                    RewardProgramScanAction.CHECK_IN,
                    RewardProgramScanAction.APPLY_CASHBACK,
                    RewardProgramScanAction.TRACK_TIER_PROGRESS,
                ),
            )
        }
    }
}
