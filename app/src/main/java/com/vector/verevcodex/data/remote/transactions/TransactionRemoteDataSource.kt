package com.vector.verevcodex.data.remote.transactions

import com.vector.verevcodex.data.remote.api.transactions.ApprovalRequestDto
import com.vector.verevcodex.data.remote.api.transactions.TransactionCommitRequestDto
import com.vector.verevcodex.data.remote.api.transactions.TransactionItemRequestDto
import com.vector.verevcodex.data.remote.api.transactions.TransactionViewDto
import com.vector.verevcodex.data.remote.api.transactions.VerevTransactionsApi
import com.vector.verevcodex.data.remote.api.transactions.VoidTransactionRequestDto
import com.vector.verevcodex.data.remote.core.parseRemoteInstant
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.data.remote.core.unwrapNullable
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequestType
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalStatus
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionStatus
import com.vector.verevcodex.domain.model.transactions.TransactionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRemoteDataSource @Inject constructor(
    private val api: VerevTransactionsApi,
) {

    suspend fun list(storeId: String?): Result<List<Transaction>> = remoteResult {
        val response = api.list()
        val list = response.unwrap { dto ->
            dto.items.orEmpty().map { it.toDomain() }.let { list ->
                if (storeId != null) list.filter { it.storeId == storeId } else list
            }
        }
        list
    }

    suspend fun get(transactionId: String): Result<Transaction?> = remoteResult {
        val response = api.get(transactionId)
        response.unwrapNullable { it.toDomain() }
    }

    suspend fun listApprovals(status: TransactionApprovalStatus? = null): Result<List<TransactionApprovalRequest>> = remoteResult {
        val response = api.listApprovals(status = status.toRemoteStatus())
        response.unwrap { approvals -> approvals.map { it.toDomain() } }
    }

    suspend fun requestVoid(transactionId: String, reason: String): Result<TransactionApprovalRequest> = remoteResult {
        val response = api.requestVoid(
            transactionId = transactionId,
            request = VoidTransactionRequestDto(reason = reason),
        )
        response.unwrap { it.toDomain() }
    }

    suspend fun commit(transaction: Transaction, idempotencyKey: String, rewardId: String? = null): Result<Transaction> = remoteResult {
        val request = TransactionCommitRequestDto(
            storeId = transaction.storeId,
            customerId = transaction.customerId,
            amount = transaction.amount,
            redeemPoints = transaction.pointsRedeemed,
            rewardId = rewardId,
            // The backend expects an ISO-8601 instant, while the app keeps a zone-less LocalDateTime.
            occurredAt = transaction.timestamp.atOffset(ZoneOffset.UTC).toInstant().toString(),
            items = transaction.items.map {
                TransactionItemRequestDto(
                    sku = it.id,
                    title = it.name,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                )
            },
            notes = null,
        )
        val response = api.commit(request, idempotencyKey)
        response.unwrap { requireNotNull(it.transaction) { "Missing committed transaction" }.toDomain() }
    }
}

private fun TransactionViewDto.toDomain() = Transaction(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    storeId = storeId.orEmpty(),
    staffId = staffUserId.orEmpty(),
    type = transactionType.toDomainTransactionType(),
    status = status.toDomainTransactionStatus(),
    amount = amount ?: 0.0,
    pointsEarned = pointsEarned ?: 0,
    pointsRedeemed = pointsRedeemed ?: 0,
    approvalRequestId = approvalRequestId,
    originalTransactionId = originalTransactionId,
    timestamp = occurredAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
    metadata = "",
    countsAsVisit = true,
    items = emptyList(), // backend list/get may not include line items
)

private fun ApprovalRequestDto.toDomain() = TransactionApprovalRequest(
    id = id.orEmpty(),
    targetTransactionId = targetTransactionId,
    requestedByUserId = requestedByUserId.orEmpty(),
    requestType = requestType.toDomainApprovalRequestType(),
    status = status.toDomainApprovalStatus(),
    reasonText = reasonText.orEmpty(),
    decisionNotes = decisionNotes,
    decidedByUserId = decidedByUserId,
    createdAt = LocalDateTime.ofInstant(parseRemoteInstant(createdAt), ZoneOffset.UTC),
    decidedAt = decidedAt?.let { LocalDateTime.ofInstant(parseRemoteInstant(it), ZoneOffset.UTC) },
)

private fun String?.toDomainTransactionType(): TransactionType = when (this?.trim()?.uppercase()) {
    "PURCHASE" -> TransactionType.PURCHASE
    "VOID" -> TransactionType.VOID
    "ENGAGEMENT" -> TransactionType.ENGAGEMENT
    else -> TransactionType.UNKNOWN
}

private fun String?.toDomainTransactionStatus(): TransactionStatus = when (this?.trim()?.uppercase()) {
    "PENDING_APPROVAL" -> TransactionStatus.PENDING_APPROVAL
    "COMPLETED" -> TransactionStatus.COMPLETED
    "REJECTED" -> TransactionStatus.REJECTED
    "VOIDED" -> TransactionStatus.VOIDED
    else -> TransactionStatus.UNKNOWN
}

private fun String?.toDomainApprovalRequestType(): TransactionApprovalRequestType = when (this?.trim()?.uppercase()) {
    "REWARD_REDEMPTION" -> TransactionApprovalRequestType.REWARD_REDEMPTION
    "MANUAL_POINTS_ADJUSTMENT" -> TransactionApprovalRequestType.MANUAL_POINTS_ADJUSTMENT
    "TRANSACTION_VOID" -> TransactionApprovalRequestType.TRANSACTION_VOID
    else -> TransactionApprovalRequestType.UNKNOWN
}

private fun String?.toDomainApprovalStatus(): TransactionApprovalStatus = when (this?.trim()?.uppercase()) {
    "PENDING" -> TransactionApprovalStatus.PENDING
    "APPROVED" -> TransactionApprovalStatus.APPROVED
    "REJECTED" -> TransactionApprovalStatus.REJECTED
    "CANCELLED" -> TransactionApprovalStatus.CANCELLED
    else -> TransactionApprovalStatus.UNKNOWN
}

private fun TransactionApprovalStatus?.toRemoteStatus(): String? = when (this) {
    TransactionApprovalStatus.PENDING -> "PENDING"
    TransactionApprovalStatus.APPROVED -> "APPROVED"
    TransactionApprovalStatus.REJECTED -> "REJECTED"
    TransactionApprovalStatus.CANCELLED -> "CANCELLED"
    TransactionApprovalStatus.UNKNOWN, null -> null
}
