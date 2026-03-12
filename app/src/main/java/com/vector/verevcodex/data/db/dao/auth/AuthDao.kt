package com.vector.verevcodex.data.db.dao.auth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthDao {
    @Query("SELECT * FROM auth_accounts WHERE id = :accountId LIMIT 1")
    fun observeById(accountId: String): Flow<AuthAccountEntity?>

    @Query("SELECT * FROM auth_accounts WHERE id = :accountId LIMIT 1")
    suspend fun findById(accountId: String): AuthAccountEntity?

    @Query("SELECT * FROM auth_accounts WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): AuthAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AuthAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AuthAccountEntity>)

    @Update
    suspend fun update(item: AuthAccountEntity)
}
