package com.vector.verevcodex.domain.model.transactions

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDateTime

enum class TransactionType {
    PURCHASE,
    VOID,
    ENGAGEMENT,
    UNKNOWN,
}

enum class TransactionStatus {
    PENDING_APPROVAL,
    COMPLETED,
    REJECTED,
    VOIDED,
    UNKNOWN,
}

data class Transaction(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val staffId: String,
    val type: TransactionType = TransactionType.PURCHASE,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val amount: Double,
    val pointsEarned: Int,
    val pointsRedeemed: Int,
    val approvalRequestId: String? = null,
    val originalTransactionId: String? = null,
    val timestamp: LocalDateTime,
    val metadata: String,
    val countsAsVisit: Boolean = true,
    val items: List<TransactionItem> = emptyList(),
) : Identifiable
