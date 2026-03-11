package com.vector.verevcodex.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vector.verevcodex.data.db.dao.auth.AuthDao
import com.vector.verevcodex.data.db.dao.CustomerBusinessRelationDao
import com.vector.verevcodex.data.db.dao.CustomerCredentialDao
import com.vector.verevcodex.data.db.dao.CustomerDao
import com.vector.verevcodex.data.db.dao.LoyaltyDao
import com.vector.verevcodex.data.db.dao.NotificationDao
import com.vector.verevcodex.data.db.dao.OwnerDao
import com.vector.verevcodex.data.db.dao.PointsLedgerDao
import com.vector.verevcodex.data.db.dao.StaffDao
import com.vector.verevcodex.data.db.dao.StoreDao
import com.vector.verevcodex.data.db.dao.TransactionDao
import com.vector.verevcodex.data.db.dao.TransactionItemDao
import com.vector.verevcodex.data.db.entity.BusinessLocationEntity
import com.vector.verevcodex.data.db.entity.CampaignEntity
import com.vector.verevcodex.data.db.entity.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.CustomerCredentialEntity
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
import com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity

@Database(
    entities = [
        OwnerEntity::class,
        StoreEntity::class,
        BusinessLocationEntity::class,
        StaffMemberEntity::class,
        CustomerEntity::class,
        CustomerCredentialEntity::class,
        CustomerBusinessRelationEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        RewardProgramEntity::class,
        RewardEntity::class,
        PointsLedgerEntity::class,
        CampaignEntity::class,
        CampaignTargetEntity::class,
        NotificationEntity::class,
        AuthAccountEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao
    abstract fun ownerDao(): OwnerDao
    abstract fun storeDao(): StoreDao
    abstract fun staffDao(): StaffDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerCredentialDao(): CustomerCredentialDao
    abstract fun customerBusinessRelationDao(): CustomerBusinessRelationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun pointsLedgerDao(): PointsLedgerDao
    abstract fun notificationDao(): NotificationDao
}
