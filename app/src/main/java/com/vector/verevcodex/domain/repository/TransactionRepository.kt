package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(storeId: String? = null): Flow<List<Transaction>>
    suspend fun recordTransaction(transaction: Transaction)
}
