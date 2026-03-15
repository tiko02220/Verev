package com.vector.verevcodex.data.db.dao.business

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY firstName, lastName")
    fun observeStaff(storeId: String?): Flow<List<StaffMemberEntity>>

    @Query("SELECT * FROM staff WHERE id = :staffId LIMIT 1")
    suspend fun getById(staffId: String): StaffMemberEntity?

    @Query("SELECT * FROM staff")
    suspend fun getAll(): List<StaffMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<StaffMemberEntity>)

    @Update
    suspend fun update(item: StaffMemberEntity)

    @Query("DELETE FROM staff WHERE id = :staffId")
    suspend fun deleteById(staffId: String)
}
