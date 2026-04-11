package com.vector.verevcodex.data.repository.transactions

import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.transactions.TransactionRemoteDataSource
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequestType
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.repository.realtime.RealtimeRepository
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemote: TransactionRemoteDataSource,
    private val realtimeRepository: RealtimeRepository,
) : TransactionRepository {

    override fun observeTransactions(storeId: String?): Flow<List<Transaction>> {
        return realtimeRepository.observeRefreshSignals()
            .onStart { emit(Unit) }
            .map { transactionRemote.list(storeId).getOrElse { emptyList() } }
    }

    override fun observeTransaction(transactionId: String): Flow<Transaction?> {
        return realtimeRepository.observeRefreshSignals()
            .onStart { emit(Unit) }
            .map { transactionRemote.get(transactionId).getOrNull() }
    }

    override fun observeTransactionVoidRequest(transactionId: String): Flow<TransactionApprovalRequest?> {
        return realtimeRepository.observeRefreshSignals()
            .onStart { emit(Unit) }
            .map {
                transactionRemote.listApprovals().getOrElse { emptyList() }
                    .asSequence()
                    .filter { approval ->
                        approval.targetTransactionId == transactionId &&
                            approval.requestType == TransactionApprovalRequestType.TRANSACTION_VOID
                    }
                    .maxByOrNull(TransactionApprovalRequest::createdAt)
            }
    }

    override suspend fun recordTransaction(transaction: Transaction, incrementVisit: Boolean, rewardId: String?) {
        val idempotencyKey = buildRemoteIdempotencyKey(
            domain = RemoteIdempotencyDomain.TRANSACTION,
            action = RemoteIdempotencyAction.CREATE,
            transaction.customerId,
            transaction.storeId,
            transaction.timestamp.toString(),
            transaction.amount.toString(),
        )
        transactionRemote.commit(
            transaction = transaction.copy(countsAsVisit = incrementVisit),
            idempotencyKey = idempotencyKey,
            rewardId = rewardId,
        ).getOrThrow()
    }

    override suspend fun requestVoidTransaction(transactionId: String, reason: String): TransactionApprovalRequest =
        transactionRemote.requestVoid(transactionId, reason).getOrThrow()
}
