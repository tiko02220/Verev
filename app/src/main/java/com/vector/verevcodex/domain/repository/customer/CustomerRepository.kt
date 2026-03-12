package com.vector.verevcodex.domain.repository.customer

import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun observeCustomers(storeId: String? = null): Flow<List<Customer>>
    fun observeCustomerRelationsByStore(storeId: String): Flow<List<CustomerBusinessRelation>>
    fun observeCustomer(customerId: String): Flow<Customer?>
    fun observeCustomerRelations(customerId: String): Flow<List<CustomerBusinessRelation>>
    fun observeCustomerCredentials(customerId: String): Flow<List<CustomerCredential>>
    fun observeCustomerPointsLedger(customerId: String): Flow<List<PointsLedger>>
    suspend fun findByLoyaltyId(loyaltyId: String): Customer?
    suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer
    suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer
    suspend fun updateCustomerContact(
        customerId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        favoriteStoreId: String?,
    )
    suspend fun updateCustomerNotesAndTags(
        customerId: String,
        storeId: String,
        notes: String,
        tags: List<String>,
    )
    suspend fun upsertCustomerCredential(
        customerId: String,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String? = null,
    )
    suspend fun adjustPoints(customerId: String, delta: Int, reason: String)
}
