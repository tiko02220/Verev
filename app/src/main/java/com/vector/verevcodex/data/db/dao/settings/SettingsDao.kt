package com.vector.verevcodex.data.db.dao.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vector.verevcodex.data.db.entity.settings.BillingInvoiceEntity
import com.vector.verevcodex.data.db.entity.settings.BranchConfigurationEntity
import com.vector.verevcodex.data.db.entity.settings.BrandingSettingsEntity
import com.vector.verevcodex.data.db.entity.settings.SavedPaymentMethodEntity
import com.vector.verevcodex.data.db.entity.settings.SubscriptionPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessSettingsDao {
    @Query("SELECT * FROM branding_settings WHERE storeId = :storeId LIMIT 1")
    fun observeBrandingSettings(storeId: String): Flow<BrandingSettingsEntity?>

    @Query("SELECT * FROM branding_settings WHERE storeId = :storeId LIMIT 1")
    suspend fun getBrandingSettings(storeId: String): BrandingSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBrandingSettings(item: BrandingSettingsEntity)

    @Query("SELECT * FROM subscription_plans WHERE ownerId = :ownerId ORDER BY active DESC, renewalDate DESC LIMIT 1")
    fun observeSubscriptionPlan(ownerId: String): Flow<SubscriptionPlanEntity?>

    @Query("SELECT * FROM subscription_plans WHERE ownerId = :ownerId ORDER BY active DESC, renewalDate DESC LIMIT 1")
    suspend fun getSubscriptionPlan(ownerId: String): SubscriptionPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubscriptionPlan(item: SubscriptionPlanEntity)

    @Query("SELECT * FROM payment_methods WHERE ownerId = :ownerId ORDER BY isDefault DESC, brand, last4")
    fun observePaymentMethods(ownerId: String): Flow<List<SavedPaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE ownerId = :ownerId ORDER BY isDefault DESC, brand, last4")
    suspend fun getPaymentMethods(ownerId: String): List<SavedPaymentMethodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPaymentMethod(item: SavedPaymentMethodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPaymentMethods(items: List<SavedPaymentMethodEntity>)

    @Query("DELETE FROM payment_methods WHERE id = :methodId")
    suspend fun deletePaymentMethod(methodId: String)

    @Query("SELECT * FROM billing_invoices WHERE ownerId = :ownerId ORDER BY issuedDate DESC")
    fun observeInvoices(ownerId: String): Flow<List<BillingInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoices(items: List<BillingInvoiceEntity>)

    @Query("SELECT * FROM branch_configurations WHERE storeId = :storeId LIMIT 1")
    fun observeBranchConfiguration(storeId: String): Flow<BranchConfigurationEntity?>

    @Query("SELECT * FROM branch_configurations WHERE storeId = :storeId LIMIT 1")
    suspend fun getBranchConfiguration(storeId: String): BranchConfigurationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBranchConfiguration(item: BranchConfigurationEntity)
}
