package com.vector.verevcodex.data.repository.transactions

import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.transactions.TransactionRemoteDataSource
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemote: TransactionRemoteDataSource,
) : TransactionRepository {

    override fun observeTransactions(storeId: String?): Flow<List<Transaction>> {
        return flow {
            emit(transactionRemote.list(storeId).getOrElse { emptyList() })
        }
    }

    override fun observeTransaction(transactionId: String): Flow<Transaction?> {
        return flow {
            emit(transactionRemote.get(transactionId).getOrNull())
        }
    }

    override suspend fun recordTransaction(transaction: Transaction, incrementVisit: Boolean) {
        val idempotencyKey = buildRemoteIdempotencyKey(
            domain = RemoteIdempotencyDomain.TRANSACTION,
            action = RemoteIdempotencyAction.CREATE,
            transaction.customerId,
            transaction.storeId,
            transaction.timestamp.toString(),
            transaction.amount.toString(),
        )
        transactionRemote.commit(transaction.copy(countsAsVisit = incrementVisit), idempotencyKey).getOrThrow()
    }
}
