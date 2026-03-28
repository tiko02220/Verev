package com.vector.verevcodex.data.remote.settings

import com.vector.verevcodex.data.remote.api.billing.BillingInvoiceViewDto
import com.vector.verevcodex.data.remote.api.billing.BillingOverviewResponseDto
import com.vector.verevcodex.data.remote.api.billing.CreatePaymentMethodRequestDto
import com.vector.verevcodex.data.remote.api.billing.UpdateCurrentSubscriptionRequestDto
import com.vector.verevcodex.data.remote.api.billing.OrganizationSubscriptionViewDto
import com.vector.verevcodex.data.remote.api.billing.PaymentMethodViewDto
import com.vector.verevcodex.data.remote.api.billing.PlanCatalogViewDto
import com.vector.verevcodex.data.remote.api.billing.VerevBillingApi
import com.vector.verevcodex.data.remote.api.store.BranchConfigurationViewDto
import com.vector.verevcodex.data.remote.api.store.BrandingSettingsViewDto
import com.vector.verevcodex.data.remote.api.store.StoreViewDto
import com.vector.verevcodex.data.remote.api.store.UpdateBranchConfigurationRequestDto
import com.vector.verevcodex.data.remote.api.store.UpdateBrandingRequestDto
import com.vector.verevcodex.data.remote.api.store.UpdateStoreRequestDto
import com.vector.verevcodex.data.remote.api.store.VerevStoresApi
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.billing.InvoiceStatus
import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.billing.SubscriptionPlanOption
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.settings.ThemeMode
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessSettingsRemoteDataSource @Inject constructor(
    private val storesApi: VerevStoresApi,
    private val billingApi: VerevBillingApi,
) {

    suspend fun getBranding(storeId: String): Result<BrandingSettings> = remoteResult {
        val branding = storesApi.branding(storeId).unwrap { it }
        val store = storesApi.get(storeId).unwrap { it }
        branding.toDomain(store)
    }

    suspend fun saveBranding(settings: BrandingSettings): Result<Unit> = remoteResult {
        val currentStore = storesApi.get(settings.storeId).unwrap { it }
        storesApi.update(
            storeId = settings.storeId,
            request = UpdateStoreRequestDto(
                name = currentStore.name.orEmpty(),
                address = currentStore.address.orEmpty(),
                contactInfo = currentStore.contactInfo.orEmpty(),
                category = currentStore.category.orEmpty(),
                workingHours = currentStore.workingHours.orEmpty(),
                logoUrl = settings.logoUri,
                primaryColor = settings.primaryColor,
                secondaryColor = settings.secondaryColor,
            ),
            idempotencyKey = storeIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                settings.storeId,
                settings.logoUri,
                settings.primaryColor,
                settings.secondaryColor,
            ),
        ).unwrap { it }
        storesApi.updateBranding(
            storeId = settings.storeId,
            request = UpdateBrandingRequestDto(
                selectedPaletteId = settings.selectedPaletteId,
                themeMode = settings.themeMode.name,
                accentColor = settings.accentColor,
            ),
            idempotencyKey = storeIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                settings.storeId,
                settings.selectedPaletteId,
                settings.themeMode.name,
                settings.accentColor,
            ),
        ).unwrap { it }
        Unit
    }

    suspend fun getBranchConfiguration(storeId: String): Result<BranchConfiguration> = remoteResult {
        storesApi.configuration(storeId).unwrap { it.toDomain() }
    }

    suspend fun saveBranchConfiguration(configuration: BranchConfiguration): Result<Unit> = remoteResult {
        storesApi.updateConfiguration(
            storeId = configuration.storeId,
            request = UpdateBranchConfigurationRequestDto(
                customerSelfEnrollmentEnabled = configuration.customerSelfEnrollmentEnabled,
                nfcCardProvisioningEnabled = configuration.nfcCardProvisioningEnabled,
                barcodeScannerEnabled = configuration.barcodeScannerEnabled,
                managerApprovalRequiredForRedemption = configuration.managerApprovalRequiredForRedemption,
                managerApprovalRequiredForPointsAdjustment = configuration.managerApprovalRequiredForPointsAdjustment,
                receiptFooter = configuration.receiptFooter,
            ),
            idempotencyKey = storeIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                configuration.storeId,
                configuration.customerSelfEnrollmentEnabled.toString(),
                configuration.nfcCardProvisioningEnabled.toString(),
                configuration.barcodeScannerEnabled.toString(),
                configuration.managerApprovalRequiredForRedemption.toString(),
                configuration.managerApprovalRequiredForPointsAdjustment.toString(),
                configuration.receiptFooter,
            ),
        ).unwrap { it }
        Unit
    }

    suspend fun getCurrentSubscription(ownerId: String): Result<SubscriptionPlan?> = remoteResult {
        billingApi.overview().unwrap { it.currentSubscription?.toDomain(ownerId) }
    }

    suspend fun getPaymentMethods(ownerId: String): Result<List<SavedPaymentMethod>> = remoteResult {
        billingApi.paymentMethods().unwrap { list -> list.map { it.toDomain(ownerId) } }
    }

    suspend fun addPaymentMethod(ownerId: String, draft: PaymentMethodDraft): Result<Unit> = remoteResult {
        billingApi.createPaymentMethod(
            request = CreatePaymentMethodRequestDto(
                brand = draft.brand.trim(),
                last4 = draft.last4,
                expiryMonth = draft.expiryMonth,
                expiryYear = draft.expiryYear,
                setDefault = draft.isDefault,
            ),
            idempotencyKey = billingIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                ownerId,
                draft.brand,
                draft.last4,
                draft.expiryMonth.toString(),
                draft.expiryYear.toString(),
                draft.isDefault.toString(),
            ),
        ).unwrap { it }
        Unit
    }

    suspend fun setDefaultPaymentMethod(ownerId: String, methodId: String): Result<Unit> = remoteResult {
        billingApi.setDefaultPaymentMethod(
            paymentMethodId = methodId,
            idempotencyKey = billingIdempotencyKey(
                action = RemoteIdempotencyAction.SET_DEFAULT,
                ownerId,
                methodId,
            ),
        ).unwrap { it }
        Unit
    }

    suspend fun removePaymentMethod(ownerId: String, methodId: String): Result<Unit> =
        remoteResult {
            billingApi.removePaymentMethod(
                paymentMethodId = methodId,
                idempotencyKey = billingIdempotencyKey(
                    action = RemoteIdempotencyAction.DELETE,
                    ownerId,
                    methodId,
                ),
            ).unwrap { Unit }
            Unit
        }

    suspend fun getInvoices(ownerId: String): Result<List<BillingInvoice>> = remoteResult {
        billingApi.invoices().unwrap { list -> list.map { it.toDomain(ownerId) } }
    }

    suspend fun getAvailablePlans(): Result<List<SubscriptionPlanOption>> = remoteResult {
        billingApi.plans().unwrap { list -> list.filter { it.active ?: false }.map { it.toDomain() } }
    }

    suspend fun updateSubscriptionPlan(ownerId: String, planId: String): Result<Unit> =
        remoteResult {
            billingApi.updateCurrentSubscription(
                request = UpdateCurrentSubscriptionRequestDto(
                    planCode = planId,
                    autoRenew = true,
                ),
                idempotencyKey = billingIdempotencyKey(
                    action = RemoteIdempotencyAction.UPDATE,
                    ownerId,
                    planId,
                ),
            ).unwrap { it }
            Unit
        }

    private fun storeIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.STORE, action, *parts)

    private fun billingIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.BILLING, action, *parts)

}

private fun BrandingSettingsViewDto.toDomain(store: StoreViewDto) = BrandingSettings(
    storeId = storeId.orEmpty(),
    selectedPaletteId = selectedPaletteId.orEmpty(),
    themeMode = ThemeMode.entries.find { it.name.equals(themeMode.orEmpty(), true) } ?: ThemeMode.LIGHT,
    primaryColor = store.primaryColor.orEmpty().ifEmpty { "#111827" },
    secondaryColor = store.secondaryColor.orEmpty().ifEmpty { "#F59E0B" },
    accentColor = accentColor.orEmpty(),
    logoUri = store.logoUrl.orEmpty(),
)

private fun BranchConfigurationViewDto.toDomain() = BranchConfiguration(
    storeId = storeId.orEmpty(),
    customerSelfEnrollmentEnabled = customerSelfEnrollmentEnabled ?: false,
    nfcCardProvisioningEnabled = nfcCardProvisioningEnabled ?: false,
    barcodeScannerEnabled = barcodeScannerEnabled ?: false,
    managerApprovalRequiredForRedemption = managerApprovalRequiredForRedemption ?: false,
    managerApprovalRequiredForPointsAdjustment = managerApprovalRequiredForPointsAdjustment ?: false,
    receiptFooter = receiptFooter.orEmpty(),
)

private fun OrganizationSubscriptionViewDto.toDomain(ownerId: String) = SubscriptionPlan(
    id = id.orEmpty(),
    ownerId = ownerId,
    name = planCode.orEmpty(),
    monthlyPrice = monthlyPrice ?: 0.0,
    currencyCode = currencyCode.orEmpty(),
    renewalDate = LocalDate.parse(renewalDate.orEmpty().ifBlank { LocalDate.now().toString() }),
    active = active ?: false,
)

private fun PaymentMethodViewDto.toDomain(ownerId: String) = SavedPaymentMethod(
    id = id.orEmpty(),
    ownerId = ownerId,
    brand = brand.orEmpty(),
    last4 = last4.orEmpty(),
    expiryMonth = expiryMonth ?: 0,
    expiryYear = expiryYear ?: 0,
    isDefault = isDefault ?: false,
)

private fun BillingInvoiceViewDto.toDomain(ownerId: String) = BillingInvoice(
    id = id.orEmpty(),
    ownerId = ownerId,
    title = title.orEmpty(),
    periodLabel = periodLabel.orEmpty(),
    amount = amount ?: 0.0,
    currencyCode = currencyCode.orEmpty(),
    status = InvoiceStatus.entries.find { it.name.equals(status.orEmpty(), true) } ?: InvoiceStatus.DUE,
    issuedDate = LocalDate.parse(issuedDate.orEmpty().ifBlank { LocalDate.now().toString() }),
)

private fun PlanCatalogViewDto.toDomain() = SubscriptionPlanOption(
    id = code.orEmpty(),
    monthlyPrice = basePrice ?: 0.0,
    currencyCode = currencyCode.orEmpty(),
)
