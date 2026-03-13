package com.vector.verevcodex.domain.repository.transactions

import com.vector.verevcodex.domain.model.transactions.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(storeId: String? = null): Flow<List<Transaction>>
    fun observeTransaction(transactionId: String): Flow<Transaction?>
    suspend fun recordTransaction(transaction: Transaction, incrementVisit: Boolean = true)
}
