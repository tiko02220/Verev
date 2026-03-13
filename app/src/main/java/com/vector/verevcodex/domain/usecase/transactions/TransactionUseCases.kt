package com.vector.verevcodex.domain.usecase.transactions

import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeTransactions(storeId)
}

class ObserveTransactionUseCase(private val repository: TransactionRepository) {
    operator fun invoke(transactionId: String) = repository.observeTransaction(transactionId)
}

class RecordTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction, incrementVisit: Boolean = true) =
        repository.recordTransaction(transaction, incrementVisit)
}
