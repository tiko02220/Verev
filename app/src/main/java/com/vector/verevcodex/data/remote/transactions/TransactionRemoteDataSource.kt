package com.vector.verevcodex.data.remote.transactions

import com.vector.verevcodex.data.remote.api.transactions.TransactionCommitRequestDto
import com.vector.verevcodex.data.remote.api.transactions.TransactionItemRequestDto
import com.vector.verevcodex.data.remote.api.transactions.TransactionViewDto
import com.vector.verevcodex.data.remote.api.transactions.VerevTransactionsApi
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.data.remote.core.unwrapNullable
import com.vector.verevcodex.domain.model.transactions.Transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRemoteDataSource @Inject constructor(
    private val api: VerevTransactionsApi,
) {

    suspend fun list(storeId: String?): Result<List<Transaction>> = runCatching {
        val response = api.list()
        val list = response.unwrap { dto ->
            dto.items.orEmpty().map { it.toDomain() }.let { list ->
                if (storeId != null) list.filter { it.storeId == storeId } else list
            }
        }
        list
    }

    suspend fun get(transactionId: String): Result<Transaction?> = runCatching {
        val response = api.get(transactionId)
        response.unwrapNullable { it.toDomain() }
    }

    suspend fun commit(transaction: Transaction, idempotencyKey: String): Result<Transaction> = runCatching {
        val request = TransactionCommitRequestDto(
            storeId = transaction.storeId,
            customerId = transaction.customerId,
            amount = transaction.amount,
            redeemPoints = transaction.pointsRedeemed,
            rewardId = null,
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
    amount = amount ?: 0.0,
    pointsEarned = pointsEarned ?: 0,
    pointsRedeemed = pointsRedeemed ?: 0,
    timestamp = occurredAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
    metadata = "",
    countsAsVisit = true,
    items = emptyList(), // backend list/get may not include line items
)
