package com.vector.verevcodex.data.repository.transactions

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.data.mapper.toEntity
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : TransactionRepository {
    override fun observeTransactions(storeId: String?): Flow<List<Transaction>> =
        database.transactionDao().observeTransactions(storeId).map { transactions ->
            val items = database.transactionItemDao().getByTransactionIds(transactions.map { it.id })
            transactions.map { tx -> tx.toDomain(items.filter { it.transactionId == tx.id }) }
        }

    override fun observeTransaction(transactionId: String): Flow<Transaction?> =
        combine(
            database.transactionDao().observeTransaction(transactionId),
            database.transactionItemDao().observeByTransactionId(transactionId),
        ) { transaction, items ->
            transaction?.toDomain(items)
        }

    override suspend fun recordTransaction(transaction: Transaction, incrementVisit: Boolean) {
        database.transactionDao().insert(transaction.toEntity())
        database.transactionItemDao().insertAll(transaction.items.map { it.toEntity() })
        val customer = database.customerDao().getCustomer(transaction.customerId) ?: return
        val updatedCustomer = customer.copy(
            totalVisits = customer.totalVisits + if (incrementVisit) 1 else 0,
            totalSpent = customer.totalSpent + transaction.amount,
            currentPoints = customer.currentPoints + transaction.pointsEarned - transaction.pointsRedeemed,
            lastVisit = if (incrementVisit) transaction.timestamp.toString() else customer.lastVisit,
            favoriteStoreId = transaction.storeId,
        )
        database.customerDao().update(updatedCustomer)
        database.pointsLedgerDao().insert(
            PointsLedgerEntity(
                UUID.randomUUID().toString(),
                transaction.customerId,
                transaction.id,
                transaction.pointsEarned - transaction.pointsRedeemed,
                "Transaction sync",
                transaction.timestamp.toString(),
            )
        )
    }
}
