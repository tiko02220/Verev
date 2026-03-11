package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.CustomerDraft
import com.vector.verevcodex.domain.repository.CustomerRepository

class ObserveCustomersUseCase(private val repository: CustomerRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeCustomers(storeId)
}

class ObserveCustomerUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomer(customerId)
}

class ObserveCustomerCredentialsUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomerCredentials(customerId)
}

class FindCustomerByLoyaltyIdUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(loyaltyId: String): Customer? = repository.findByLoyaltyId(loyaltyId)
}

class CreateCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(draft: CustomerDraft, storeId: String): Customer = repository.createCustomer(draft, storeId)
}

class QuickRegisterCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer {
        return repository.registerQuickCustomer(firstName, phoneNumber, loyaltyId, storeId)
    }
}

class UpsertCustomerCredentialUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(
        customerId: String,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String? = null,
    ) = repository.upsertCustomerCredential(customerId, loyaltyId, method, status, referenceValue)
}

class AdjustCustomerPointsUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, delta: Int, reason: String) = repository.adjustPoints(customerId, delta, reason)
}
