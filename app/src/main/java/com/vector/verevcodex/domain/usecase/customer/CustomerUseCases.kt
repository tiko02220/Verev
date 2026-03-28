package com.vector.verevcodex.domain.usecase.customer

import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.model.customer.CustomerMergePreview
import com.vector.verevcodex.domain.model.customer.CustomerMergeResult
import com.vector.verevcodex.domain.model.customer.CustomerSplitPreview
import com.vector.verevcodex.domain.model.customer.CustomerSplitResult
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.repository.customer.CustomerRepository

class ObserveCustomersUseCase(private val repository: CustomerRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeCustomers(storeId)
}

class ObserveCustomerUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomer(customerId)
}

class ObserveCustomerRelationsByStoreUseCase(private val repository: CustomerRepository) {
    operator fun invoke(storeId: String) = repository.observeCustomerRelationsByStore(storeId)
}

class ObserveCustomerRelationsUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomerRelations(customerId)
}

class ObserveCustomerCredentialsUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomerCredentials(customerId)
}

class ObserveCustomerPointsLedgerUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomerPointsLedger(customerId)
}

class ObserveCustomerBonusActionsUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomerBonusActions(customerId)
}

class FindCustomerByLoyaltyIdUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(loyaltyId: String, storeId: String? = null): Customer? =
        repository.findByLoyaltyId(loyaltyId, storeId)
}

class CreateCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(draft: CustomerDraft, storeId: String): Customer = repository.createCustomer(draft, storeId)
}

class QuickRegisterCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer {
        return repository.registerQuickCustomer(firstName, phoneNumber, loyaltyId, storeId)
    }
}

class UpdateCustomerContactUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(
        customerId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        favoriteStoreId: String?,
    ) = repository.updateCustomerContact(customerId, firstName, lastName, phoneNumber, email, favoriteStoreId)
}

class UpdateCustomerNotesAndTagsUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(
        customerId: String,
        storeId: String,
        notes: String,
        tags: List<String>,
    ) = repository.updateCustomerNotesAndTags(customerId, storeId, notes, tags)
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

class AdjustCustomerVisitsUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, delta: Int, reason: String) = repository.adjustVisits(customerId, delta, reason)
}

class RecordCustomerCheckInUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, storeId: String) =
        repository.recordCheckIn(customerId, storeId)
}

class RecordCustomerBonusActionUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(
        customerId: String,
        storeId: String?,
        type: CustomerBonusActionType,
        title: String,
        details: String,
    ) = repository.recordBonusAction(customerId, storeId, type, title, details)
}

class PreviewCustomerMergeUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(sourceCustomerId: String, targetCustomerId: String): CustomerMergePreview =
        repository.previewCustomerMerge(sourceCustomerId, targetCustomerId)
}

class MergeCustomersUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(sourceCustomerId: String, targetCustomerId: String, notes: String? = null): CustomerMergeResult =
        repository.mergeCustomers(sourceCustomerId, targetCustomerId, notes)
}

class PreviewCustomerSplitUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(sourceCustomerId: String): CustomerSplitPreview =
        repository.previewCustomerSplit(sourceCustomerId)
}

class SplitCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(
        sourceCustomerId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        notes: String? = null,
    ): CustomerSplitResult = repository.splitCustomer(
        sourceCustomerId = sourceCustomerId,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        email = email,
        notes = notes,
    )
}
