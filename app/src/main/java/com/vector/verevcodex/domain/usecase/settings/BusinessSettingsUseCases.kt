package com.vector.verevcodex.domain.usecase.settings

import com.vector.verevcodex.domain.model.billing.PaymentMethodDraft
import com.vector.verevcodex.domain.model.settings.BranchConfiguration
import com.vector.verevcodex.domain.model.settings.BrandingSettings
import com.vector.verevcodex.domain.repository.settings.BusinessSettingsRepository

class ObserveBrandingSettingsUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke(storeId: String) = repository.observeBrandingSettings(storeId)
}

class SaveBrandingSettingsUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(settings: BrandingSettings) = repository.saveBrandingSettings(settings)
}

class ObserveSubscriptionPlanUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke(ownerId: String) = repository.observeSubscriptionPlan(ownerId)
}

class ObservePaymentMethodsUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke(ownerId: String) = repository.observePaymentMethods(ownerId)
}

class AddPaymentMethodUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(ownerId: String, draft: PaymentMethodDraft) = repository.addPaymentMethod(ownerId, draft)
}

class SetDefaultPaymentMethodUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(ownerId: String, methodId: String) = repository.setDefaultPaymentMethod(ownerId, methodId)
}

class RemovePaymentMethodUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(ownerId: String, methodId: String) = repository.removePaymentMethod(ownerId, methodId)
}

class ObserveInvoicesUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke(ownerId: String) = repository.observeInvoices(ownerId)
}

class ObserveAvailableSubscriptionPlansUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke() = repository.observeAvailablePlans()
}

class UpdateSubscriptionPlanUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(ownerId: String, planId: String) = repository.updateSubscriptionPlan(ownerId, planId)
}

class ObserveBranchConfigurationUseCase(private val repository: BusinessSettingsRepository) {
    operator fun invoke(storeId: String) = repository.observeBranchConfiguration(storeId)
}

class SaveBranchConfigurationUseCase(private val repository: BusinessSettingsRepository) {
    suspend operator fun invoke(configuration: BranchConfiguration) = repository.saveBranchConfiguration(configuration)
}
