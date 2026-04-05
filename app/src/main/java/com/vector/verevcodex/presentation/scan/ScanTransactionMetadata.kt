package com.vector.verevcodex.presentation.scan

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction

internal object ScanTransactionMetadata {
    private const val DEFAULT_PURCHASE_SCAN_PROGRAM = "loyalty_scan"
    private const val DEFAULT_CHECK_IN_PROGRAM = "check_in"
    private const val PURCHASE_SUFFIX = "purchase_scan"
    private const val CHECK_IN_SUFFIX = "visit_check_in"
    const val CASHBACK_REASON = "cashback_credit_scan_flow"
    const val REWARD_REDEMPTION_REASON = "reward_redemption_scan_flow"

    fun purchase(program: RewardProgram?): String = "${program?.name ?: DEFAULT_PURCHASE_SCAN_PROGRAM}_$PURCHASE_SUFFIX"

    fun checkIn(program: RewardProgram?): String = "${program?.name ?: DEFAULT_CHECK_IN_PROGRAM}_$CHECK_IN_SUFFIX"

    fun defaultProgramName(action: RewardProgramScanAction): String = when (action) {
        RewardProgramScanAction.EARN_POINTS -> DEFAULT_PURCHASE_SCAN_PROGRAM
        RewardProgramScanAction.REDEEM_REWARDS -> "reward_redemption"
        RewardProgramScanAction.CHECK_IN -> DEFAULT_CHECK_IN_PROGRAM
        RewardProgramScanAction.TRACK_TIER_PROGRESS -> "tier_progress"
    }
}
