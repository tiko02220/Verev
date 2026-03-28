package com.vector.verevcodex.domain.model.transactions

import java.time.LocalDateTime

enum class TransactionApprovalRequestType {
    REWARD_REDEMPTION,
    MANUAL_POINTS_ADJUSTMENT,
    TRANSACTION_VOID,
    UNKNOWN,
}

enum class TransactionApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    UNKNOWN,
}

data class TransactionApprovalRequest(
    val id: String,
    val targetTransactionId: String?,
    val requestedByUserId: String,
    val requestType: TransactionApprovalRequestType,
    val status: TransactionApprovalStatus,
    val reasonText: String,
    val decisionNotes: String? = null,
    val decidedByUserId: String? = null,
    val createdAt: LocalDateTime,
    val decidedAt: LocalDateTime? = null,
)
