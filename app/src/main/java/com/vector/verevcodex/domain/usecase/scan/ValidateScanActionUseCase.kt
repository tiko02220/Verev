package com.vector.verevcodex.domain.usecase.scan

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import javax.inject.Inject

sealed class ScanValidationError {
    object InvalidAmount : ScanValidationError()
    object InvalidPoints : ScanValidationError()
    object PointsExceedBalance : ScanValidationError()
    data class MinimumPointsRequired(val min: Int) : ScanValidationError()
    data class MinimumSpendRequired(val min: Double) : ScanValidationError()
}

/**
 * Senior Approach: Encapsulates domain validation rules for scanning actions.
 * Decouples business logic from UI-level ViewModels.
 */
class ValidateScanActionUseCase @Inject constructor() {
    operator fun invoke(
        action: RewardProgramScanAction,
        amount: Double?,
        points: Int?,
        customerPoints: Int,
        activePrograms: List<RewardProgram>
    ): ScanValidationError? {
        return when (action) {
            RewardProgramScanAction.EARN_POINTS -> {
                if (amount == null || amount <= 0.0) ScanValidationError.InvalidAmount else null
            }
            RewardProgramScanAction.REDEEM_REWARDS -> {
                val minPoints = activePrograms.sumOf { it.configuration.pointsRule.minimumRedeemPoints }
                when {
                    points == null || points <= 0 -> ScanValidationError.InvalidPoints
                    points > customerPoints -> ScanValidationError.PointsExceedBalance
                    points < minPoints -> ScanValidationError.MinimumPointsRequired(minPoints)
                    else -> null
                }
            }
            RewardProgramScanAction.APPLY_CASHBACK -> {
                val minSpend = activePrograms.maxOfOrNull { it.configuration.cashbackRule.minimumSpendAmount } ?: 0.0
                when {
                    amount == null || amount <= 0.0 -> ScanValidationError.InvalidAmount
                    amount < minSpend -> ScanValidationError.MinimumSpendRequired(minSpend)
                    else -> null
                }
            }
            else -> null
        }
    }
}
