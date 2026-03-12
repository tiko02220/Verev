package com.vector.verevcodex.data.db.dao.customer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.customer.CustomerBonusActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerBonusActionDao {
    @Query("SELECT * FROM customer_bonus_actions WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeActions(customerId: String): Flow<List<CustomerBonusActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerBonusActionEntity)
}
