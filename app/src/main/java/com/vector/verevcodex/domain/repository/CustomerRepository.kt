package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerCredential
import com.vector.verevcodex.domain.model.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.CustomerDraft
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun observeCustomers(storeId: String? = null): Flow<List<Customer>>
    fun observeCustomer(customerId: String): Flow<Customer?>
    fun observeCustomerCredentials(customerId: String): Flow<List<CustomerCredential>>
    suspend fun findByLoyaltyId(loyaltyId: String): Customer?
    suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer
    suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer
    suspend fun upsertCustomerCredential(
        customerId: String,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String? = null,
    )
    suspend fun adjustPoints(customerId: String, delta: Int, reason: String)
}
