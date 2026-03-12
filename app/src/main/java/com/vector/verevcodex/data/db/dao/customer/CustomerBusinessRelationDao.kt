package com.vector.verevcodex.data.db.dao.customer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.customer.CustomerBusinessRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerBusinessRelationDao {
    @Query("SELECT * FROM customer_business_relations WHERE storeId = :storeId ORDER BY joinedAt DESC")
    fun observeRelationsByStore(storeId: String): Flow<List<CustomerBusinessRelationEntity>>

    @Query("SELECT * FROM customer_business_relations WHERE customerId = :customerId ORDER BY joinedAt DESC")
    fun observeRelations(customerId: String): Flow<List<CustomerBusinessRelationEntity>>

    @Query("SELECT * FROM customer_business_relations WHERE customerId = :customerId AND storeId = :storeId LIMIT 1")
    suspend fun getRelation(customerId: String, storeId: String): CustomerBusinessRelationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerBusinessRelationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerBusinessRelationEntity>)
}
