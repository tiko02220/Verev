package com.vector.verevcodex.domain.model.loyalty

data class RewardProgramConfiguration(
    val earningEnabled: Boolean,
    val rewardRedemptionEnabled: Boolean,
    val visitCheckInEnabled: Boolean,
    val cashbackEnabled: Boolean,
    val tierTrackingEnabled: Boolean,
    val couponEnabled: Boolean,
    val purchaseFrequencyEnabled: Boolean,
    val referralEnabled: Boolean,
    val scanActions: Set<RewardProgramScanAction>,
    val pointsRule: PointsProgramRule = PointsProgramRule(),
    val cashbackRule: CashbackProgramRule = CashbackProgramRule(),
    val tierRule: TierProgramRule = TierProgramRule(),
    val couponRule: CouponProgramRule = CouponProgramRule(),
    val checkInRule: CheckInProgramRule = CheckInProgramRule(),
    val purchaseFrequencyRule: PurchaseFrequencyProgramRule = PurchaseFrequencyProgramRule(),
    val referralRule: ReferralProgramRule = ReferralProgramRule(),
)
