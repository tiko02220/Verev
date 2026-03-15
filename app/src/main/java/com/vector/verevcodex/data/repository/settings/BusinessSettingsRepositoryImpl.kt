package com.vector.verevcodex.data.repository.settings

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.settings.BranchConfigurationEntity
import com.vector.verevcodex.data.db.entity.settings.BrandingSettingsEntity
import com.vector.verevcodex.data.db.entity.settings.SavedPaymentMethodEntity
import com.vector.verevcodex.data.db.entity.settings.SubscriptionPlanEntity
import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.billing.SubscriptionPlanOption
import com.vector.verevcodex.domain.model.settings.ThemeMode
import com.vector.verevcodex.domain.repository.settings.BusinessSettingsRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class BusinessSettingsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : BusinessSettingsRepository {
    override fun observeBrandingSettings(storeId: String): Flow<BrandingSettings?> =
        combine(
            database.businessSettingsDao().observeBrandingSettings(storeId),
            database.storeDao().observeStore(storeId),
        ) { entity, store -> entity?.toDomain(store) }

    override suspend fun saveBrandingSettings(settings: BrandingSettings): Result<Unit> = runCatching {
        val store = database.storeDao().getStore(settings.storeId) ?: error("Store not found")
        database.storeDao().insert(
            store.copy(
                primaryColor = settings.primaryColor,
                secondaryColor = settings.secondaryColor,
            )
        )
        database.businessSettingsDao().upsertBrandingSettings(
            BrandingSettingsEntity(
                storeId = settings.storeId,
                selectedPaletteId = settings.selectedPaletteId,
                themeMode = settings.themeMode.name,
                accentColor = settings.accentColor,
                logoUri = settings.logoUri,
            )
        )
    }

    override fun observeSubscriptionPlan(ownerId: String): Flow<SubscriptionPlan?> =
        database.businessSettingsDao().observeSubscriptionPlan(ownerId).map { it?.toDomain() }

    override fun observePaymentMethods(ownerId: String): Flow<List<SavedPaymentMethod>> =
        database.businessSettingsDao().observePaymentMethods(ownerId).map { items -> items.map { it.toDomain() } }

    override suspend fun addPaymentMethod(ownerId: String, draft: PaymentMethodDraft): Result<Unit> = runCatching {
        ensureOwnerBillingDefaults(ownerId)
        require(draft.brand.isNotBlank()) { "invalid_payment_method_brand" }
        require(draft.last4.length == 4 && draft.last4.all(Char::isDigit)) { "invalid_payment_method_last4" }
        require(draft.expiryMonth in 1..12) { "invalid_payment_method_expiry_month" }
        require(draft.expiryYear >= LocalDate.now().year) { "invalid_payment_method_expiry_year" }

        val existing = database.businessSettingsDao().getPaymentMethods(ownerId)
        val shouldBeDefault = draft.isDefault || existing.isEmpty()
        val normalized = existing.map { it.copy(isDefault = if (shouldBeDefault) false else it.isDefault) }
        if (shouldBeDefault && normalized.isNotEmpty()) {
            database.businessSettingsDao().upsertPaymentMethods(normalized)
        }
        database.businessSettingsDao().upsertPaymentMethod(
            SavedPaymentMethodEntity(
                id = "pm_${UUID.randomUUID()}",
                ownerId = ownerId,
                brand = draft.brand.trim(),
                last4 = draft.last4,
                expiryMonth = draft.expiryMonth,
                expiryYear = draft.expiryYear,
                isDefault = shouldBeDefault,
            )
        )
    }

    override suspend fun setDefaultPaymentMethod(ownerId: String, methodId: String): Result<Unit> = runCatching {
        val updated = database.businessSettingsDao().getPaymentMethods(ownerId).map { it.copy(isDefault = it.id == methodId) }
        database.businessSettingsDao().upsertPaymentMethods(updated)
    }

    override suspend fun removePaymentMethod(ownerId: String, methodId: String): Result<Unit> = runCatching {
        database.businessSettingsDao().deletePaymentMethod(methodId)
        val remaining = database.businessSettingsDao().getPaymentMethods(ownerId)
        if (remaining.isNotEmpty() && remaining.none { it.isDefault }) {
            database.businessSettingsDao().upsertPaymentMethods(
                remaining.mapIndexed { index, method -> method.copy(isDefault = index == 0) }
            )
        }
    }

    override fun observeInvoices(ownerId: String): Flow<List<BillingInvoice>> =
        database.businessSettingsDao().observeInvoices(ownerId).map { items -> items.map { it.toDomain() } }

    override fun observeAvailablePlans(): Flow<List<SubscriptionPlanOption>> = flowOf(BillingPlanCatalog.plans)

    override suspend fun updateSubscriptionPlan(ownerId: String, planId: String): Result<Unit> = runCatching {
        val selectedPlan = BillingPlanCatalog.find(planId) ?: error("invalid_subscription_plan")
        database.businessSettingsDao().upsertSubscriptionPlan(
            SubscriptionPlanEntity(
                id = "plan_$ownerId",
                ownerId = ownerId,
                name = selectedPlan.id,
                monthlyPrice = selectedPlan.monthlyPrice,
                currencyCode = selectedPlan.currencyCode,
                renewalDate = LocalDate.now().plusMonths(1).toString(),
                active = true,
            )
        )
    }

    override fun observeBranchConfiguration(storeId: String): Flow<BranchConfiguration?> =
        database.businessSettingsDao().observeBranchConfiguration(storeId).map { it?.toDomain() }

    override suspend fun saveBranchConfiguration(configuration: BranchConfiguration): Result<Unit> = runCatching {
        database.businessSettingsDao().upsertBranchConfiguration(configuration.toEntity())
    }

    suspend fun createDefaultStoreSettings(storeId: String, ownerId: String, primaryColor: String, secondaryColor: String) {
        if (database.businessSettingsDao().getBrandingSettings(storeId) == null) {
            database.businessSettingsDao().upsertBrandingSettings(
                BrandingSettingsEntity(
                    storeId = storeId,
                    selectedPaletteId = BusinessSettingsDefaults.defaultPaletteId,
                    themeMode = ThemeMode.LIGHT.name,
                    accentColor = secondaryColor,
                    logoUri = "",
                )
            )
        }
        if (database.businessSettingsDao().getBranchConfiguration(storeId) == null) {
            database.businessSettingsDao().upsertBranchConfiguration(
                BranchConfigurationEntity(
                    storeId = storeId,
                    customerSelfEnrollmentEnabled = true,
                    nfcCardProvisioningEnabled = true,
                    barcodeScannerEnabled = true,
                    managerApprovalRequiredForRedemption = false,
                    managerApprovalRequiredForPointsAdjustment = true,
                    receiptFooter = BusinessSettingsDefaults.defaultReceiptFooter,
                )
            )
        }
        ensureOwnerBillingDefaults(ownerId)
        val store = database.storeDao().getStore(storeId)
        if (store != null && (store.primaryColor != primaryColor || store.secondaryColor != secondaryColor)) {
            database.storeDao().insert(store.copy(primaryColor = primaryColor, secondaryColor = secondaryColor))
        }
    }

    private suspend fun ensureOwnerBillingDefaults(ownerId: String) {
        if (database.businessSettingsDao().getSubscriptionPlan(ownerId) == null) {
            database.businessSettingsDao().upsertSubscriptionPlan(
                SubscriptionPlanEntity(
                    id = "plan_$ownerId",
                    ownerId = ownerId,
                    name = BusinessSettingsDefaults.subscriptionPlanId,
                    monthlyPrice = BillingPlanCatalog.find(BusinessSettingsDefaults.subscriptionPlanId)?.monthlyPrice ?: 99.0,
                    currencyCode = BillingPlanCatalog.find(BusinessSettingsDefaults.subscriptionPlanId)?.currencyCode ?: "AMD",
                    renewalDate = LocalDate.now().plusMonths(1).toString(),
                    active = true,
                )
            )
        }
    }
}
