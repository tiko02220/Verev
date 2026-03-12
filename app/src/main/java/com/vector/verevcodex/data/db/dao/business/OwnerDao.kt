package com.vector.verevcodex.data.db.dao.business

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.business.OwnerEntity

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners LIMIT 1")
    suspend fun getOwner(): OwnerEntity

    @Query("SELECT * FROM owners WHERE id = :ownerId LIMIT 1")
    suspend fun getOwnerById(ownerId: String): OwnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OwnerEntity>)
}
