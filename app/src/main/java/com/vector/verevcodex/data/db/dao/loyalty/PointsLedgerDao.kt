package com.vector.verevcodex.data.db.dao.loyalty

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsLedgerDao {
    @Query("SELECT * FROM points_ledger WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun observeByCustomerId(customerId: String): Flow<List<PointsLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PointsLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PointsLedgerEntity>)
}
