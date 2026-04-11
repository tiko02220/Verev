package com.vector.verevcodex.domain.repository.transactions

import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.model.transactions.TransactionApprovalRequest
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(storeId: String? = null): Flow<List<Transaction>>
    fun observeTransaction(transactionId: String): Flow<Transaction?>
    fun observeTransactionVoidRequest(transactionId: String): Flow<TransactionApprovalRequest?>
    suspend fun recordTransaction(transaction: Transaction, incrementVisit: Boolean = true, rewardId: String? = null)
    suspend fun requestVoidTransaction(transactionId: String, reason: String): TransactionApprovalRequest
}
