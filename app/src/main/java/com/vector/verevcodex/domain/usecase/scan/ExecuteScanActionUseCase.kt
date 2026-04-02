package com.vector.verevcodex.domain.usecase.scan

import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.customer.Customer
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
        customer: Customer,
        activePrograms: List<RewardProgram>,
        visitAlreadyCounted: Boolean,
        metadata: String = ""
    ): Result<Unit> = runCatching {
        val shouldAutoApplyCheckIn =
            action != RewardProgramScanAction.CHECK_IN &&
                !visitAlreadyCounted &&
                activePrograms.any { program ->
                    program.active &&
                        program.configuration.visitCheckInEnabled &&
                        RewardProgramScanAction.CHECK_IN in program.configuration.scanActions
                }
        when (action) {
            RewardProgramScanAction.EARN_POINTS -> {
                val purchaseAmount = amount ?: throw IllegalArgumentException("Amount required for earning")
                val earnedPoints = calculateTotalEarnedPoints(activePrograms, purchaseAmount, customer)
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
                if (shouldAutoApplyCheckIn) {
                    customerRepository.recordCheckIn(customerId, storeId)
                }
            }
            RewardProgramScanAction.REDEEM_REWARDS -> {
                val pointsToRedeem = points ?: throw IllegalArgumentException("Points required for redemption")
                customerRepository.adjustPoints(customerId, -pointsToRedeem, "Reward Redemption")
                if (shouldAutoApplyCheckIn) {
                    customerRepository.recordCheckIn(customerId, storeId)
                }
            }
            RewardProgramScanAction.CHECK_IN -> {
                customerRepository.recordCheckIn(customerId, storeId)
            }
            else -> { /* Other actions like tier progress are handled here */ }
        }
    }

    private fun calculateTotalEarnedPoints(programs: List<RewardProgram>, amount: Double, customer: Customer): Int {
        val earnProgram = programs
            .asSequence()
            .filter { program ->
                program.active &&
                    RewardProgramScanAction.EARN_POINTS in program.configuration.scanActions &&
                    program.type in setOf(
                        LoyaltyProgramType.POINTS,
                        LoyaltyProgramType.TIER,
                        LoyaltyProgramType.HYBRID,
                    )
            }
            .sortedBy { program ->
                when (program.type) {
                    LoyaltyProgramType.POINTS -> 0
                    LoyaltyProgramType.TIER -> 1
                    LoyaltyProgramType.HYBRID -> 2
                    else -> 9
                }
            }
            .firstOrNull()
        val basePoints = earnProgram?.configuration?.pointsRule?.let { rule ->
            if (rule.spendStepAmount > 0) {
                (amount / rule.spendStepAmount).toInt() * rule.pointsAwardedPerStep
            } else {
                0
            }
        } ?: 0
        val tierBonusPercent = programs
            .asSequence()
            .filter { it.active && it.configuration.tierTrackingEnabled }
            .map { it.configuration.tierRule.activeBonusPercent(customer.currentPoints, customer.totalSpent) }
            .maxOrNull()
            ?: 0
        return basePoints + ((basePoints * tierBonusPercent) / 100)
    }
}
