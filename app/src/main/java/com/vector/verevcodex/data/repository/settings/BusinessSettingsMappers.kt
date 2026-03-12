package com.vector.verevcodex.data.repository.settings

import com.vector.verevcodex.data.db.entity.settings.BillingInvoiceEntity
import com.vector.verevcodex.data.db.entity.settings.BranchConfigurationEntity
import com.vector.verevcodex.data.db.entity.settings.BrandingSettingsEntity
import com.vector.verevcodex.data.db.entity.business.StoreEntity
import com.vector.verevcodex.data.db.entity.settings.SavedPaymentMethodEntity
import com.vector.verevcodex.data.db.entity.settings.SubscriptionPlanEntity
import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.billing.InvoiceStatus
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.settings.ThemeMode
import java.time.LocalDate

internal fun BrandingSettingsEntity.toDomain(store: StoreEntity?) = BrandingSettings(
    storeId = storeId,
    selectedPaletteId = selectedPaletteId,
    themeMode = ThemeMode.valueOf(themeMode),
    primaryColor = store?.primaryColor ?: BusinessSettingsDefaults.defaultPrimaryColor,
    secondaryColor = store?.secondaryColor ?: BusinessSettingsDefaults.defaultSecondaryColor,
    accentColor = accentColor,
)

internal fun SubscriptionPlanEntity.toDomain() = SubscriptionPlan(
    id = id,
    ownerId = ownerId,
    name = name,
    monthlyPrice = monthlyPrice,
    currencyCode = currencyCode,
    renewalDate = LocalDate.parse(renewalDate),
    active = active,
)

internal fun SavedPaymentMethodEntity.toDomain() = SavedPaymentMethod(
    id = id,
    ownerId = ownerId,
    brand = brand,
    last4 = last4,
    expiryMonth = expiryMonth,
    expiryYear = expiryYear,
    isDefault = isDefault,
)

internal fun BillingInvoiceEntity.toDomain() = BillingInvoice(
    id = id,
    ownerId = ownerId,
    title = title,
    periodLabel = periodLabel,
    amount = amount,
    currencyCode = currencyCode,
    status = InvoiceStatus.valueOf(status),
    issuedDate = LocalDate.parse(issuedDate),
)

internal fun BranchConfigurationEntity.toDomain() = BranchConfiguration(
    storeId = storeId,
    customerSelfEnrollmentEnabled = customerSelfEnrollmentEnabled,
    nfcCardProvisioningEnabled = nfcCardProvisioningEnabled,
    barcodeScannerEnabled = barcodeScannerEnabled,
    managerApprovalRequiredForRedemption = managerApprovalRequiredForRedemption,
    managerApprovalRequiredForPointsAdjustment = managerApprovalRequiredForPointsAdjustment,
    receiptFooter = receiptFooter,
)

internal fun BranchConfiguration.toEntity() = BranchConfigurationEntity(
    storeId = storeId,
    customerSelfEnrollmentEnabled = customerSelfEnrollmentEnabled,
    nfcCardProvisioningEnabled = nfcCardProvisioningEnabled,
    barcodeScannerEnabled = barcodeScannerEnabled,
    managerApprovalRequiredForRedemption = managerApprovalRequiredForRedemption,
    managerApprovalRequiredForPointsAdjustment = managerApprovalRequiredForPointsAdjustment,
    receiptFooter = receiptFooter,
)
