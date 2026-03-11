package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.repository.TransactionRepository

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeTransactions(storeId)
}

class RecordTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.recordTransaction(transaction)
}
