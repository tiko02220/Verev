package com.vector.verevcodex.data.db.dao.customer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.customer.CustomerCredentialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerCredentialDao {
    @Query("SELECT * FROM customer_credentials WHERE customerId = :customerId ORDER BY method")
    fun observeCredentials(customerId: String): Flow<List<CustomerCredentialEntity>>

    @Query("SELECT * FROM customer_credentials WHERE customerId = :customerId AND method = :method LIMIT 1")
    suspend fun getCredential(customerId: String, method: String): CustomerCredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerCredentialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerCredentialEntity>)
}
