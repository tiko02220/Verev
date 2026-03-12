package com.vector.verevcodex.domain.repository.settings

import com.vector.verevcodex.domain.model.billing.BillingInvoice
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.model.billing.SavedPaymentMethod
import com.vector.verevcodex.domain.model.billing.SubscriptionPlan
import com.vector.verevcodex.domain.model.billing.SubscriptionPlanOption
import kotlinx.coroutines.flow.Flow

interface BusinessSettingsRepository {
    fun observeBrandingSettings(storeId: String): Flow<BrandingSettings?>
    suspend fun saveBrandingSettings(settings: BrandingSettings): Result<Unit>
    fun observeSubscriptionPlan(ownerId: String): Flow<SubscriptionPlan?>
    fun observePaymentMethods(ownerId: String): Flow<List<SavedPaymentMethod>>
    suspend fun addPaymentMethod(ownerId: String, draft: PaymentMethodDraft): Result<Unit>
    suspend fun setDefaultPaymentMethod(ownerId: String, methodId: String): Result<Unit>
    suspend fun removePaymentMethod(ownerId: String, methodId: String): Result<Unit>
    fun observeInvoices(ownerId: String): Flow<List<BillingInvoice>>
    fun observeAvailablePlans(): Flow<List<SubscriptionPlanOption>>
    suspend fun updateSubscriptionPlan(ownerId: String, planId: String): Result<Unit>
    fun observeBranchConfiguration(storeId: String): Flow<BranchConfiguration?>
    suspend fun saveBranchConfiguration(configuration: BranchConfiguration): Result<Unit>
}
