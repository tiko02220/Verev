package com.vector.verevcodex.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vector.verevcodex.data.db.entity.CampaignEntity
import com.vector.verevcodex.data.db.entity.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.CustomerEntity
import com.vector.verevcodex.data.db.entity.NotificationEntity
import com.vector.verevcodex.data.db.entity.OwnerEntity
import com.vector.verevcodex.data.db.entity.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.RewardEntity
import com.vector.verevcodex.data.db.entity.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.StoreEntity
import com.vector.verevcodex.data.db.entity.TransactionEntity
import com.vector.verevcodex.data.db.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners LIMIT 1")
    suspend fun getOwner(): OwnerEntity

    @Query("SELECT * FROM owners WHERE id = :ownerId LIMIT 1")
    suspend fun getOwnerById(ownerId: String): OwnerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OwnerEntity>)
}

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
}

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
}

@Dao
interface CustomerDao {
    @Query("SELECT c.* FROM customers c INNER JOIN customer_business_relations r ON r.customerId = c.id WHERE (:storeId IS NULL OR r.storeId = :storeId) ORDER BY c.firstName, c.lastName")
    fun observeCustomers(storeId: String?): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    fun observeCustomer(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE nfcId = :nfcId LIMIT 1")
    suspend fun findByNfcId(nfcId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getCustomer(customerId: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerEntity>)

    @Update
    suspend fun update(item: CustomerEntity)
}

@Dao
interface CustomerBusinessRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerBusinessRelationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerBusinessRelationEntity>)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY timestamp DESC")
    fun observeTransactions(storeId: String?): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionEntity>)
}

@Dao
interface TransactionItemDao {
    @Query("SELECT * FROM transaction_items WHERE transactionId IN (:transactionIds)")
    suspend fun getByTransactionIds(transactionIds: List<String>): List<TransactionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TransactionItemEntity>)
}

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM reward_programs WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY name")
    fun observePrograms(storeId: String?): Flow<List<RewardProgramEntity>>

    @Query("SELECT * FROM rewards WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY pointsRequired")
    fun observeRewards(storeId: String?): Flow<List<RewardEntity>>

    @Query("SELECT * FROM campaigns WHERE (:storeId IS NULL OR storeId = :storeId) ORDER BY startDate DESC")
    fun observeCampaigns(storeId: String?): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaign_targets")
    suspend fun getCampaignTargets(): List<CampaignTargetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(items: List<RewardProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(items: List<RewardEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(items: List<CampaignEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaignTargets(items: List<CampaignTargetEntity>)
}

@Dao
interface PointsLedgerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PointsLedgerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PointsLedgerEntity>)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificationEntity>)
}
