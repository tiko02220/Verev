package com.vector.verevcodex.data.remote.customer

import com.vector.verevcodex.data.remote.api.customer.*
import com.vector.verevcodex.data.remote.core.*
import com.vector.verevcodex.data.mapper.toCustomer
import com.vector.verevcodex.data.mapper.toRelation
import com.vector.verevcodex.data.mapper.toCredential
import com.vector.verevcodex.data.mapper.toBonusAction
import com.vector.verevcodex.data.mapper.toPointsLedger
import com.vector.verevcodex.domain.model.customer.*
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRemoteDataSource @Inject constructor(
    private val api: VerevCustomersApi,
) {
    suspend fun list(storeId: String?, limit: Int = 100): Result<List<Customer>> = runCatching {
        api.list(storeId = storeId, limit = limit).unwrap { list -> list.map { it.toCustomer() } }
    }

    suspend fun get(customerId: String): Result<Customer?> = runCatching {
        api.get(customerId).unwrapNullable { it.toCustomer() }
    }

    suspend fun getDetail(customerId: String): Result<CustomerDetailResponseDto?> = runCatching {
        api.get(customerId).unwrapNullable { it }
    }

    suspend fun create(draft: CustomerDraft, storeId: String): Result<Customer> = runCatching {
        val request = CreateCustomerRequestDto(
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            phoneNumber = draft.phoneNumber.trim(),
            email = draft.email.trim().takeIf { it.isNotBlank() },
            birthDate = null,
            homeStoreId = storeId,
            tags = emptyList(),
            notes = null
        )
        api.create(request, customerIdempotencyKey(RemoteIdempotencyAction.CREATE, storeId, draft.phoneNumber))
            .unwrap { it.toCustomer() }
    }

    suspend fun quickRegister(firstName: String, phoneNumber: String, storeId: String): Result<Customer> = runCatching {
        val request = QuickRegisterCustomerRequestDto(
            firstName = firstName,
            lastName = "",
            phoneNumber = phoneNumber,
            email = null,
            birthDate = null,
            homeStoreId = storeId
        )
        api.quickRegister(request, customerIdempotencyKey(RemoteIdempotencyAction.QUICK_REGISTER, storeId, phoneNumber))
            .unwrap { it.toCustomer() }
    }

    suspend fun updateContact(
        customerId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        homeStoreId: String?,
        version: Long,
    ): Result<Customer> = runCatching {
        val request = UpdateCustomerRequestDto(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email.takeIf { it.isNotBlank() },
            birthDate = null,
            homeStoreId = homeStoreId,
            version = version,
        )
        api.update(
            customerId = customerId,
            request = request,
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.UPDATE, customerId, version.toString()),
        ).unwrap { it.toCustomer() }
    }

    suspend fun memberships(customerId: String): Result<List<CustomerBusinessRelation>> = runCatching {
        api.memberships(customerId).unwrap { list -> list.map { it.toRelation() } }
    }

    suspend fun upsertMembership(customerId: String, storeId: String, notes: String, tags: List<String>): Result<CustomerBusinessRelation> = runCatching {
        api.upsertMembership(customerId, storeId, UpdateMembershipRequestDto(notes, tags), customerIdempotencyKey(RemoteIdempotencyAction.UPSERT, customerId, storeId))
            .unwrap { it.toRelation() }
    }

    suspend fun credentials(customerId: String): Result<List<CustomerCredential>> = runCatching {
        api.credentials(customerId).unwrap { list -> list.map { it.toCredential() } }
    }

    suspend fun createCredential(customerId: String, method: CustomerCredentialMethod, referenceValue: String?): Result<CustomerCredential> = runCatching {
        api.createCredential(customerId, CreateCustomerCredentialRequestDto(method.name, referenceValue), customerIdempotencyKey(RemoteIdempotencyAction.CREATE, customerId, method.name))
            .unwrap { it.toCredential() }
    }

    suspend fun patchCredential(customerId: String, credentialId: String, status: CustomerCredentialStatus, referenceValue: String?): Result<CustomerCredential> = runCatching {
        api.patchCredential(customerId, credentialId, PatchCustomerCredentialRequestDto(status.name, referenceValue), customerIdempotencyKey(RemoteIdempotencyAction.PATCH, credentialId, status.name))
            .unwrap { it.toCredential() }
    }

    suspend fun bonusActions(customerId: String): Result<List<CustomerBonusAction>> = runCatching {
        api.bonusActions(customerId).unwrap { list -> list.map { it.toBonusAction() } }
    }

    suspend fun createBonusAction(
        customerId: String,
        storeId: String?,
        type: CustomerBonusActionType,
        title: String,
        details: String,
    ): Result<CustomerBonusAction> = runCatching {
        api.createBonusAction(
            customerId = customerId,
            request = CreateCustomerBonusActionRequestDto(storeId = storeId, type = type.name, title = title, details = details),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.CREATE, customerId, type.name, title),
        ).unwrap { it.toBonusAction() }
    }

    suspend fun pointsLedger(customerId: String, limit: Int = 100): Result<List<PointsLedger>> = runCatching {
        api.pointsLedger(customerId, limit).unwrap { list -> list.map { it.toPointsLedger() } }
    }

    suspend fun adjustPoints(customerId: String, storeId: String, delta: Int, reason: String): Result<Unit> = runCatching {
        api.adjustPoints(
            customerId = customerId,
            request = ManualPointsAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.ADJUST_POINTS, customerId, storeId, delta.toString()),
        ).unwrap { Unit }
    }

    suspend fun adjustVisits(customerId: String, storeId: String, delta: Int, reason: String): Result<Unit> = runCatching {
        api.adjustVisits(
            customerId = customerId,
            request = ManualVisitAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.ADJUST_VISITS, customerId, storeId, delta.toString()),
        ).unwrap { Unit }
    }

    suspend fun findByLoyaltyId(loyaltyId: String): Result<Customer?> = runCatching {
        api.byLoyaltyId(loyaltyId).unwrapNullable { it.toCustomer() }
    }

    private fun customerIdempotencyKey(action: RemoteIdempotencyAction, vararg parts: String?): String = 
        buildRemoteIdempotencyKey(domain = RemoteIdempotencyDomain.CUSTOMER, action = action, *parts)
}
