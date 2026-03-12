package com.vector.verevcodex.data.db.dao.customer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vector.verevcodex.data.db.entity.customer.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT c.* FROM customers c INNER JOIN customer_business_relations r ON r.customerId = c.id WHERE (:storeId IS NULL OR r.storeId = :storeId) ORDER BY c.firstName, c.lastName")
    fun observeCustomers(storeId: String?): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    fun observeCustomer(customerId: String): Flow<CustomerEntity?>

    @Query(
        """
        SELECT c.* FROM customers c
        LEFT JOIN customer_credentials cc ON cc.customerId = c.id
        WHERE c.nfcId = :identifier OR cc.loyaltyId = :identifier OR cc.referenceValue = :identifier
        LIMIT 1
        """
    )
    suspend fun findByLoyaltyId(identifier: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getCustomer(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerEntity>)

    @Update
    suspend fun update(item: CustomerEntity)
}
