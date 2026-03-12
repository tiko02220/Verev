package com.vector.verevcodex.data.db.dao.business

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY name")
    fun observeStores(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
    fun observeStore(storeId: String): Flow<StoreEntity?>

    @Query("SELECT * FROM stores WHERE id = :storeId LIMIT 1")
    suspend fun getStore(storeId: String): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StoreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StoreEntity)
}
