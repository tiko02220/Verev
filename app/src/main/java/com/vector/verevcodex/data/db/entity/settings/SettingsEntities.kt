package com.vector.verevcodex.data.db.entity.settings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branding_settings")
data class BrandingSettingsEntity(
    @PrimaryKey val storeId: String,
    val selectedPaletteId: String,
    val themeMode: String,
    val accentColor: String,
    val logoUri: String,
)

@Entity(tableName = "subscription_plans")
data class SubscriptionPlanEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val monthlyPrice: Double,
    val currencyCode: String,
    val renewalDate: String,
    val active: Boolean,
)

@Entity(tableName = "payment_methods")
data class SavedPaymentMethodEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val brand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
)

@Entity(tableName = "billing_invoices")
data class BillingInvoiceEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val periodLabel: String,
    val amount: Double,
    val currencyCode: String,
    val status: String,
    val issuedDate: String,
)

@Entity(tableName = "branch_configurations")
data class BranchConfigurationEntity(
    @PrimaryKey val storeId: String,
    val customerSelfEnrollmentEnabled: Boolean,
    val nfcCardProvisioningEnabled: Boolean,
    val barcodeScannerEnabled: Boolean,
    val managerApprovalRequiredForRedemption: Boolean,
    val managerApprovalRequiredForPointsAdjustment: Boolean,
    val receiptFooter: String,
)
