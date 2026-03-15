package com.vector.verevcodex.presentation.promotions

import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.presentation.merchant.common.formatRelativeDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

internal data class PromotionPerformanceSummary(
    val usageCount: Int = 0,
    val revenue: Double = 0.0,
    val customerCount: Int = 0,
    val redemptionRate: Double = 0.0,
)

internal data class PromotionDailyPerformance(
    val label: String,
    val usageCount: Int,
    val revenue: Double,
)

internal data class PromotionRedemptionSummary(
    val id: String,
    val customerName: String,
    val amount: Double,
    val timeLabel: String,
)

private val promotionDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

internal fun Campaign.performanceSummary(
    transactions: List<Transaction>,
    totalStoreTransactions: Int = transactions.size,
): PromotionPerformanceSummary {
    val matchedTransactions = transactions.filter(::matchesTransaction)
    val customerCount = matchedTransactions.map(Transaction::customerId).distinct().size
    val usageCount = matchedTransactions.size
    val redemptionRate = if (totalStoreTransactions == 0) {
        0.0
    } else {
        (usageCount.toDouble() / totalStoreTransactions.toDouble()) * 100.0
    }
    return PromotionPerformanceSummary(
        usageCount = usageCount,
        revenue = matchedTransactions.sumOf(Transaction::amount),
        customerCount = customerCount,
        redemptionRate = redemptionRate,
    )
}

internal fun Campaign.weeklyPerformance(
    transactions: List<Transaction>,
    endDate: LocalDate = LocalDate.now(),
): List<PromotionDailyPerformance> {
    val startDate = endDate.minusDays(6)
    val matchedTransactions = transactions.filter(::matchesTransaction)
    return (0L..6L).map { offset ->
        val day = startDate.plusDays(offset)
        val dayTransactions = matchedTransactions.filter { it.timestamp.toLocalDate() == day }
        PromotionDailyPerformance(
            label = day.dayOfWeek.name.take(3).lowercase().replaceFirstChar(Char::titlecase),
            usageCount = dayTransactions.size,
            revenue = dayTransactions.sumOf(Transaction::amount),
        )
    }
}

internal fun Campaign.recentRedemptions(
    transactions: List<Transaction>,
    customers: List<Customer>,
): List<PromotionRedemptionSummary> {
    val customerNames = customers.associateBy(Customer::id)
    return transactions
        .filter(::matchesTransaction)
        .sortedByDescending(Transaction::timestamp)
        .take(5)
        .map { transaction ->
            val customer = customerNames[transaction.customerId]
            PromotionRedemptionSummary(
                id = transaction.id,
                customerName = customer?.fullName().orEmpty().ifBlank { "Customer" },
                amount = transaction.amount,
                timeLabel = formatRelativeDateTime(transaction.timestamp),
            )
        }
}

internal fun Campaign.matchesTransaction(transaction: Transaction): Boolean =
    transaction.metadata.contains(id) || transaction.metadata.contains(name, ignoreCase = true)

internal fun Campaign.dateRangeText(): String =
    "${startDate.format(promotionDateFormatter)} - ${endDate.format(promotionDateFormatter)}"

internal fun Campaign.endDateText(): String = endDate.format(promotionDateFormatter)

internal fun Campaign.startDateText(): String = startDate.format(promotionDateFormatter)

internal fun formatPromotionRate(value: Double): String = "${value.roundToInt()}%"

private fun Customer.fullName(): String = listOf(firstName, lastName).joinToString(" ").trim()
