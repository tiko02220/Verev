package com.vector.verevcodex.data.db.dao.transactions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.loyalty.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {
    @Query("SELECT * FROM transaction_items WHERE transactionId IN (:transactionIds)")
    suspend fun getByTransactionIds(transactionIds: List<String>): List<TransactionItemEntity>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun observeByTransactionId(transactionId: String): Flow<List<TransactionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionItemEntity>)
}
