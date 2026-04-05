package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.LoyaltyProgramType

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
                couponEnabled = false,
                purchaseFrequencyEnabled = false,
                referralEnabled = false,
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
                couponEnabled = false,
                purchaseFrequencyEnabled = false,
                referralEnabled = false,
                scanActions = setOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.REDEEM_REWARDS),
                pointsRule = PointsProgramRule(),
            )
            LoyaltyProgramType.DIGITAL_STAMP -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = true,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                couponEnabled = false,
                purchaseFrequencyEnabled = false,
                referralEnabled = false,
                scanActions = setOf(RewardProgramScanAction.CHECK_IN),
                checkInRule = CheckInProgramRule(),
            )
            LoyaltyProgramType.TIER -> RewardProgramConfiguration(
                earningEnabled = true,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = true,
                couponEnabled = false,
                purchaseFrequencyEnabled = false,
                referralEnabled = false,
                scanActions = setOf(RewardProgramScanAction.EARN_POINTS, RewardProgramScanAction.TRACK_TIER_PROGRESS),
                pointsRule = PointsProgramRule(),
                tierRule = TierProgramRule(),
            )
            LoyaltyProgramType.COUPON -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = true,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                couponEnabled = true,
                purchaseFrequencyEnabled = false,
                referralEnabled = false,
                scanActions = setOf(RewardProgramScanAction.REDEEM_REWARDS),
                couponRule = CouponProgramRule(),
            )
            LoyaltyProgramType.PURCHASE_FREQUENCY -> RewardProgramConfiguration(
                earningEnabled = true,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                couponEnabled = false,
                purchaseFrequencyEnabled = true,
                referralEnabled = false,
                scanActions = emptySet(),
                purchaseFrequencyRule = PurchaseFrequencyProgramRule(),
            )
            LoyaltyProgramType.REFERRAL -> RewardProgramConfiguration(
                earningEnabled = false,
                rewardRedemptionEnabled = false,
                visitCheckInEnabled = false,
                cashbackEnabled = false,
                tierTrackingEnabled = false,
                couponEnabled = false,
                purchaseFrequencyEnabled = false,
                referralEnabled = true,
                scanActions = emptySet(),
                referralRule = ReferralProgramRule(),
            )
        }
    }
}
