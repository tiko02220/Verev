package com.vector.verevcodex.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vector.verevcodex.data.db.dao.auth.AuthDao
import com.vector.verevcodex.data.db.dao.customer.CustomerBonusActionDao
import com.vector.verevcodex.data.db.dao.customer.CustomerBusinessRelationDao
import com.vector.verevcodex.data.db.dao.customer.CustomerCredentialDao
import com.vector.verevcodex.data.db.dao.customer.CustomerDao
import com.vector.verevcodex.data.db.dao.loyalty.LoyaltyDao
import com.vector.verevcodex.data.db.dao.notifications.NotificationDao
import com.vector.verevcodex.data.db.dao.business.OwnerDao
import com.vector.verevcodex.data.db.dao.loyalty.PointsLedgerDao
import com.vector.verevcodex.data.db.dao.business.StaffDao
import com.vector.verevcodex.data.db.dao.business.StoreDao
import com.vector.verevcodex.data.db.dao.settings.BusinessSettingsDao
import com.vector.verevcodex.data.db.dao.transactions.TransactionDao
import com.vector.verevcodex.data.db.dao.transactions.TransactionItemDao
import com.vector.verevcodex.data.db.entity.settings.BillingInvoiceEntity
import com.vector.verevcodex.data.db.entity.business.BusinessLocationEntity
import com.vector.verevcodex.data.db.entity.loyalty.CampaignEntity
import com.vector.verevcodex.data.db.entity.loyalty.CampaignTargetEntity
import com.vector.verevcodex.data.db.entity.settings.BranchConfigurationEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBonusActionEntity
import com.vector.verevcodex.data.db.entity.settings.BrandingSettingsEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerCredentialEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerEntity
import com.vector.verevcodex.data.db.entity.loyalty.NotificationEntity
import com.vector.verevcodex.data.db.entity.business.OwnerEntity
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardEntity
import com.vector.verevcodex.data.db.entity.loyalty.RewardProgramEntity
import com.vector.verevcodex.data.db.entity.settings.SavedPaymentMethodEntity
import com.vector.verevcodex.data.db.entity.business.StaffMemberEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.settings.SubscriptionPlanEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionEntity
import com.vector.verevcodex.data.db.entity.loyalty.TransactionItemEntity
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
        CustomerBonusActionEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class,
        RewardProgramEntity::class,
        RewardEntity::class,
        PointsLedgerEntity::class,
        CampaignEntity::class,
        CampaignTargetEntity::class,
        NotificationEntity::class,
        AuthAccountEntity::class,
        BrandingSettingsEntity::class,
        SubscriptionPlanEntity::class,
        SavedPaymentMethodEntity::class,
        BillingInvoiceEntity::class,
        BranchConfigurationEntity::class,
    ],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao
    abstract fun ownerDao(): OwnerDao
    abstract fun storeDao(): StoreDao
    abstract fun staffDao(): StaffDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerCredentialDao(): CustomerCredentialDao
    abstract fun customerBusinessRelationDao(): CustomerBusinessRelationDao
    abstract fun customerBonusActionDao(): CustomerBonusActionDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionItemDao(): TransactionItemDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun pointsLedgerDao(): PointsLedgerDao
    abstract fun notificationDao(): NotificationDao
    abstract fun businessSettingsDao(): BusinessSettingsDao
}
