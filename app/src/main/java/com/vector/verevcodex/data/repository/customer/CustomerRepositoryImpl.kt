package com.vector.verevcodex.data.repository.customer

import com.vector.verevcodex.data.remote.customer.CustomerRemoteDataSource
import com.vector.verevcodex.data.remote.engagement.CheckInRemoteDataSource
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.repository.customer.CustomerRepository
import com.vector.verevcodex.domain.repository.store.StoreRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val customerRemote: CustomerRemoteDataSource,
    private val checkInRemote: CheckInRemoteDataSource,
    private val storeRepository: StoreRepository,
) : CustomerRepository {
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeCustomers(storeId: String?): Flow<List<Customer>> = refreshRequests
        .onStart { emit(Unit) }
        .map { customerRemote.list(storeId).getOrElse { emptyList() } }

    override fun observeCustomerRelationsByStore(storeId: String): Flow<List<CustomerBusinessRelation>> = 
        observeCustomers(storeId).map { customers ->
            customers.map { customer ->
                CustomerBusinessRelation(
                    id = "${customer.id}:$storeId",
                    customerId = customer.id,
                    storeId = storeId,
                    joinedAt = customer.enrolledDate.atStartOfDay(),
                    notes = "",
                    tags = emptyList(),
                )
            }
        }

    override fun observeCustomer(customerId: String): Flow<Customer?> = flow {
        emit(customerRemote.get(customerId).getOrNull())
    }

    override fun observeCustomerRelations(customerId: String): Flow<List<CustomerBusinessRelation>> = flow {
        emit(customerRemote.memberships(customerId).getOrElse { emptyList() })
    }

    override fun observeCustomerCredentials(customerId: String): Flow<List<CustomerCredential>> = flow {
        emit(customerRemote.credentials(customerId).getOrElse { emptyList() })
    }

    override fun observeCustomerPointsLedger(customerId: String): Flow<List<PointsLedger>> = flow {
        emit(customerRemote.pointsLedger(customerId).getOrElse { emptyList() })
    }

    override fun observeCustomerBonusActions(customerId: String): Flow<List<CustomerBonusAction>> = flow {
        emit(customerRemote.bonusActions(customerId).getOrElse { emptyList() })
    }

    override suspend fun findByLoyaltyId(loyaltyId: String): Customer? = customerRemote.findByLoyaltyId(loyaltyId).getOrNull()

    override suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer =
        customerRemote.create(draft, storeId).getOrThrow().also { refreshRequests.tryEmit(Unit) }

    override suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer =
        customerRemote.quickRegister(firstName, phoneNumber, storeId).getOrThrow().also { refreshRequests.tryEmit(Unit) }

    override suspend fun updateCustomerContact(
        customerId: String, firstName: String, lastName: String, phoneNumber: String, email: String, favoriteStoreId: String?,
    ) {
        val detail = customerRemote.getDetail(customerId).getOrNull() ?: return
        customerRemote.updateContact(
            customerId = customerId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phoneNumber = phoneNumber.trim(),
            email = email.trim().lowercase(),
            homeStoreId = favoriteStoreId,
            version = detail.profile?.version ?: 0L,
        ).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun updateCustomerNotesAndTags(customerId: String, storeId: String, notes: String, tags: List<String>) {
        customerRemote.upsertMembership(customerId, storeId, notes.trim(), tags).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun upsertCustomerCredential(
        customerId: String,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String?,
    ) {
        val existing = customerRemote.credentials(customerId).getOrElse { emptyList() }
        val target = existing.firstOrNull { it.method == method }

        if (target == null) {
            customerRemote.createCredential(customerId, method, referenceValue).getOrThrow()
        } else {
            // Orchestration: Only patch if there is a meaningful change to prevent redundant API calls
            if (target.status != status || target.referenceValue != referenceValue) {
                // In a real POS system, we would resolve the internal credential ID here
                // For now, we assume the remote handles resolution or we'd fetch details
                customerRemote.patchCredential(customerId, method.name, status, referenceValue).getOrThrow()
            }
        }
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun adjustPoints(customerId: String, delta: Int, reason: String) {
        val storeId = storeRepository.observeSelectedStore().first()?.id ?: return
        customerRemote.adjustPoints(customerId, storeId, delta, reason).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun adjustVisits(customerId: String, delta: Int, reason: String) {
        val storeId = storeRepository.observeSelectedStore().first()?.id ?: return
        customerRemote.adjustVisits(customerId, storeId, delta, reason).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun recordCheckIn(customerId: String, storeId: String, rewardPoints: Int) {
        checkInRemote.create(storeId = storeId, customerId = customerId).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }

    override suspend fun recordBonusAction(customerId: String, storeId: String?, type: CustomerBonusActionType, title: String, details: String) {
        customerRemote.createBonusAction(customerId, storeId, type, title, details).getOrThrow()
        refreshRequests.tryEmit(Unit)
    }
}
