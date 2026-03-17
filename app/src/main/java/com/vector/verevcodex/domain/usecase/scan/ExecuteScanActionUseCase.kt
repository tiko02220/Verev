package com.vector.verevcodex.domain.usecase.scan

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionItem
import com.vector.verevcodex.domain.repository.customer.CustomerRepository
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * A Domain-level orchestrator for processing scan actions.
 * Extracts business logic from the ViewModel to ensure it is reusable and testable.
 */
class ExecuteScanActionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository,
) {
    suspend operator fun invoke(
        action: RewardProgramScanAction,
        customerId: String,
        storeId: String,
        staffId: String,
        amount: Double?,
        points: Int?,
        activePrograms: List<RewardProgram>,
        visitAlreadyCounted: Boolean,
        metadata: String = ""
    ): Result<Unit> = runCatching {
        when (action) {
            RewardProgramScanAction.EARN_POINTS -> {
                val purchaseAmount = amount ?: throw IllegalArgumentException("Amount required for earning")
                val earnedPoints = calculateTotalEarnedPoints(activePrograms, purchaseAmount)
                val transactionId = UUID.randomUUID().toString()
                
                transactionRepository.recordTransaction(
                    Transaction(
                        id = transactionId,
                        customerId = customerId,
                        storeId = storeId,
                        staffId = staffId,
                        amount = purchaseAmount,
                        pointsEarned = earnedPoints,
                        pointsRedeemed = 0,
                        timestamp = LocalDateTime.now(),
                        metadata = metadata,
                        items = listOf(TransactionItem(UUID.randomUUID().toString(), transactionId, "Points Earning", 1, purchaseAmount))
                    ),
                    incrementVisit = !visitAlreadyCounted
                )
            }
            RewardProgramScanAction.REDEEM_REWARDS -> {
                val pointsToRedeem = points ?: throw IllegalArgumentException("Points required for redemption")
                customerRepository.adjustPoints(customerId, -pointsToRedeem, "Reward Redemption")
            }
            RewardProgramScanAction.CHECK_IN -> {
                val rewardPoints = activePrograms.firstOrNull()?.configuration?.checkInRule?.rewardPoints ?: 0
                customerRepository.recordCheckIn(customerId, storeId, rewardPoints)
            }
            else -> { /* Other actions like tier progress are handled here */ }
        }
    }

    private fun calculateTotalEarnedPoints(programs: List<RewardProgram>, amount: Double): Int {
        return programs.sumOf { program ->
            val rule = program.configuration.pointsRule
            if (rule.spendStepAmount > 0) {
                (amount / rule.spendStepAmount).toInt() * rule.pointsAwardedPerStep
            } else 0
        }
    }
}
