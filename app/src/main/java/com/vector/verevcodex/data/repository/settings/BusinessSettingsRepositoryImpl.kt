package com.vector.verevcodex.data.repository.settings

import com.vector.verevcodex.data.remote.settings.BusinessSettingsRemoteDataSource
import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.billing.SubscriptionPlanOption
import com.vector.verevcodex.domain.repository.settings.BusinessSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map

@Singleton
class BusinessSettingsRepositoryImpl @Inject constructor(
    private val remote: BusinessSettingsRemoteDataSource,
) : BusinessSettingsRepository {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeBrandingSettings(storeId: String): Flow<BrandingSettings?> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getBranding(storeId).getOrNull() }
    }

    override suspend fun saveBrandingSettings(settings: BrandingSettings): Result<Unit> = runCatching {
        remote.saveBranding(settings).getOrThrow()
    }

    override fun observeSubscriptionPlan(ownerId: String): Flow<SubscriptionPlan?> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getCurrentSubscription(ownerId).getOrNull() }
    }

    override fun observePaymentMethods(ownerId: String): Flow<List<SavedPaymentMethod>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getPaymentMethods(ownerId).getOrElse { emptyList() } }
    }

    override suspend fun addPaymentMethod(ownerId: String, draft: PaymentMethodDraft): Result<Unit> = runCatching {
        remote.addPaymentMethod(ownerId, draft).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun setDefaultPaymentMethod(ownerId: String, methodId: String): Result<Unit> = runCatching {
        remote.setDefaultPaymentMethod(ownerId, methodId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun removePaymentMethod(ownerId: String, methodId: String): Result<Unit> = runCatching {
        remote.removePaymentMethod(ownerId, methodId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override fun observeInvoices(ownerId: String): Flow<List<BillingInvoice>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getInvoices(ownerId).getOrElse { emptyList() } }
    }

    override fun observeAvailablePlans(): Flow<List<SubscriptionPlanOption>> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getAvailablePlans().getOrElse { emptyList() } }
    }

    override suspend fun updateSubscriptionPlan(ownerId: String, planId: String): Result<Unit> = runCatching {
        remote.updateSubscriptionPlan(ownerId, planId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override fun observeBranchConfiguration(storeId: String): Flow<BranchConfiguration?> {
        return refreshRequests
            .onStart { emit(Unit) }
            .map { remote.getBranchConfiguration(storeId).getOrNull() }
    }

    override suspend fun saveBranchConfiguration(configuration: BranchConfiguration): Result<Unit> = runCatching {
        remote.saveBranchConfiguration(configuration).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    suspend fun createDefaultStoreSettings(storeId: String, ownerId: String, primaryColor: String, secondaryColor: String) {
        // Backend is the source of truth for store defaults.
    }
}
