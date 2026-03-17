package com.vector.verevcodex.data.remote.customer

import com.vector.verevcodex.data.remote.api.customer.CustomerBonusActionViewDto
import com.vector.verevcodex.data.remote.api.customer.CustomerCredentialViewDto
import com.vector.verevcodex.data.remote.api.customer.CustomerDetailResponseDto
import com.vector.verevcodex.data.remote.api.customer.CustomerMembershipViewDto
import com.vector.verevcodex.data.remote.api.customer.CreateCustomerCredentialRequestDto
import com.vector.verevcodex.data.remote.api.customer.CreateCustomerBonusActionRequestDto
import com.vector.verevcodex.data.remote.api.customer.ManualPointsAdjustmentRequestDto
import com.vector.verevcodex.data.remote.api.customer.PatchCustomerCredentialRequestDto
import com.vector.verevcodex.data.remote.api.customer.ManualVisitAdjustmentRequestDto
import com.vector.verevcodex.data.remote.api.customer.PointsLedgerEntryDto
import com.vector.verevcodex.data.remote.api.customer.QuickRegisterCustomerRequestDto
import com.vector.verevcodex.data.remote.api.customer.UpdateMembershipRequestDto
import com.vector.verevcodex.data.remote.api.customer.VerevCustomersApi
import com.vector.verevcodex.data.remote.auth.ApiException
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.data.remote.core.unwrapNullable
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRemoteDataSource @Inject constructor(
    private val api: VerevCustomersApi,
) {

    suspend fun list(storeId: String?, limit: Int = 100): Result<List<Customer>> = runCatching {
        val response = api.list(storeId = storeId, limit = limit)
        response.unwrap { list -> list.map { it.toCustomer() } }
    }

    suspend fun get(customerId: String): Result<Customer?> = runCatching {
        val response = api.get(customerId)
        response.unwrapNullable { it.toCustomer() }
    }

    /** Returns full detail including profile version for updates. */
    suspend fun getDetail(customerId: String): Result<CustomerDetailResponseDto?> = runCatching {
        val response = api.get(customerId)
        response.unwrapNullable { it }
    }

    suspend fun create(draft: CustomerDraft, storeId: String): Result<Customer> = runCatching {
        val request = com.vector.verevcodex.data.remote.api.customer.CreateCustomerRequestDto(
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            phoneNumber = draft.phoneNumber.trim(),
            email = draft.email.trim().takeIf { it.isNotBlank() },
            birthDate = null,
            homeStoreId = storeId,
            tags = emptyList(),
            notes = null,
        )
        val response = api.create(
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                storeId,
                draft.firstName,
                draft.lastName,
                draft.phoneNumber,
                draft.email,
            ),
        )
        response.unwrap { it.toCustomer() }
    }

    suspend fun quickRegister(firstName: String, phoneNumber: String, storeId: String): Result<Customer> = runCatching {
        val request = QuickRegisterCustomerRequestDto(
            firstName = firstName.trim(),
            lastName = "",
            phoneNumber = phoneNumber.trim(),
            email = null,
            birthDate = null,
            homeStoreId = storeId,
        )
        val response = api.quickRegister(
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.QUICK_REGISTER,
                storeId,
                firstName,
                phoneNumber,
            ),
        )
        response.unwrap { it.toCustomer() }
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
        val request = com.vector.verevcodex.data.remote.api.customer.UpdateCustomerRequestDto(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email.takeIf { it.isNotBlank() },
            birthDate = null,
            homeStoreId = homeStoreId,
            version = version,
        )
        val response = api.update(
            customerId = customerId,
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                customerId,
                version.toString(),
                firstName,
                lastName,
                phoneNumber,
                email,
                homeStoreId,
            ),
        )
        response.unwrap { it.toCustomer() }
    }

    suspend fun memberships(customerId: String): Result<List<CustomerBusinessRelation>> = runCatching {
        val response = api.memberships(customerId)
        response.unwrap { list -> list.map { it.toRelation() } }
    }

    suspend fun upsertMembership(customerId: String, storeId: String, notes: String, tags: List<String>): Result<CustomerBusinessRelation> = runCatching {
        val request = UpdateMembershipRequestDto(notes = notes, tags = tags)
        val response = api.upsertMembership(
            customerId = customerId,
            storeId = storeId,
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.UPSERT,
                customerId,
                storeId,
                notes,
                tags.joinToString("|"),
            ),
        )
        response.unwrap { it.toRelation() }
    }

    suspend fun credentials(customerId: String): Result<List<CustomerCredential>> = runCatching {
        val response = api.credentials(customerId)
        response.unwrap { list -> list.map { it.toCredential() } }
    }

    suspend fun upsertCredential(
        customerId: String,
        existingCredentials: List<CustomerCredential>,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String?,
    ): Result<CustomerCredential> = runCatching {
        val existing = existingCredentials.firstOrNull { it.method == method }
        if (existing == null) {
            val createResponse = api.createCredential(
                customerId = customerId,
                request = CreateCustomerCredentialRequestDto(
                    method = method.name,
                    referenceValue = referenceValue,
                ),
                idempotencyKey = customerIdempotencyKey(
                    action = RemoteIdempotencyAction.CREATE,
                    customerId,
                    method.name,
                    referenceValue,
                ),
            )
            val created = createResponse.unwrap { it.toCredential() }
            if (created.status == status && (referenceValue == null || created.referenceValue == referenceValue)) {
                return@runCatching created
            }
            val refreshed = listCredentialDtos(customerId)
            val createdRecord = refreshed.firstOrNull { it.method.orEmpty().trim().uppercase() == method.name }
                ?: throw ApiException(500, "Created credential was not returned by backend for $loyaltyId")
            val patchResponse = api.patchCredential(
                customerId = customerId,
                credentialId = createdRecord.id ?: throw ApiException(500, "Created credential id missing for $loyaltyId"),
                request = PatchCustomerCredentialRequestDto(
                    status = status.name,
                    referenceValue = referenceValue,
                ),
                idempotencyKey = customerIdempotencyKey(
                    action = RemoteIdempotencyAction.PATCH,
                    customerId,
                    createdRecord.id,
                    status.name,
                    referenceValue,
                ),
            )
            return@runCatching patchResponse.unwrap { it.toCredential() }
        }

        val patchResponse = api.patchCredential(
            customerId = customerId,
            credentialId = listCredentialDtos(customerId)
                .firstOrNull { it.method.orEmpty().trim().uppercase() == method.name }
                ?.id
                ?: throw ApiException(404, "Credential not found for method $method"),
            request = PatchCustomerCredentialRequestDto(
                status = status.name,
                referenceValue = referenceValue,
            ),
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.PATCH,
                customerId,
                method.name,
                status.name,
                referenceValue,
            ),
        )
        patchResponse.unwrap { it.toCredential() }
    }

    suspend fun bonusActions(customerId: String): Result<List<CustomerBonusAction>> = runCatching {
        val response = api.bonusActions(customerId)
        response.unwrap { list -> list.map { it.toBonusAction() } }
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
            request = CreateCustomerBonusActionRequestDto(
                storeId = storeId,
                type = type.name,
                title = title,
                details = details,
            ),
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.CREATE,
                customerId,
                storeId,
                type.name,
                title,
                details,
            ),
        ).unwrap { it.toBonusAction() }
    }

    suspend fun pointsLedger(customerId: String, limit: Int = 100): Result<List<PointsLedger>> = runCatching {
        val response = api.pointsLedger(customerId, limit)
        response.unwrap { list -> list.map { it.toPointsLedger() } }
    }

    suspend fun adjustPoints(customerId: String, storeId: String, delta: Int, reason: String): Result<Unit> = runCatching {
        val request = ManualPointsAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason)
        val response = api.adjustPoints(
            customerId = customerId,
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.ADJUST_POINTS,
                customerId,
                storeId,
                delta.toString(),
                reason,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun adjustVisits(customerId: String, storeId: String, delta: Int, reason: String): Result<Unit> = runCatching {
        val request = ManualVisitAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason)
        val response = api.adjustVisits(
            customerId = customerId,
            request = request,
            idempotencyKey = customerIdempotencyKey(
                action = RemoteIdempotencyAction.ADJUST_VISITS,
                customerId,
                storeId,
                delta.toString(),
                reason,
            ),
        )
        response.unwrap { Unit }
    }

    suspend fun findByLoyaltyId(loyaltyId: String): Result<Customer?> = runCatching {
        val response = api.byLoyaltyId(loyaltyId)
        response.unwrapNullable { it.toCustomer() }
    }

    private suspend fun listCredentialDtos(customerId: String): List<CustomerCredentialViewDto> {
        val response = api.credentials(customerId)
        return response.unwrap { it }
    }

    private fun customerIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(
        domain = RemoteIdempotencyDomain.CUSTOMER,
        action = action,
        *parts,
    )
}

private fun CustomerDetailResponseDto.toCustomer() = Customer(
    id = customer?.id.orEmpty(),
    firstName = customer?.firstName.orEmpty(),
    lastName = customer?.lastName.orEmpty(),
    phoneNumber = customer?.phoneNumber.orEmpty(),
    email = customer?.email.orEmpty(),
    loyaltyId = customer?.loyaltyId.orEmpty(),
    enrolledDate = runCatching { LocalDate.parse(customer?.enrolledDate.orEmpty().take(10)) }.getOrElse { LocalDate.now() },
    totalVisits = profile?.totalVisits ?: 0,
    totalSpent = profile?.totalSpent ?: 0.0,
    currentPoints = profile?.currentPoints ?: 0,
    loyaltyTier = kotlin.runCatching { LoyaltyTier.valueOf(profile?.loyaltyTier.orEmpty().trim().uppercase()) }.getOrElse { LoyaltyTier.BRONZE },
    lastVisit = null,
    favoriteStoreId = profile?.homeStoreId,
)

private fun CustomerMembershipViewDto.toRelation() = CustomerBusinessRelation(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    storeId = storeId.orEmpty(),
    joinedAt = (joinedAt?.let { runCatching { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) }.getOrNull() } ?: LocalDateTime.now()),
    notes = notes.orEmpty(),
    tags = tags.orEmpty(),
)

private fun CustomerCredentialViewDto.toCredential() = CustomerCredential(
    customerId = customerId.orEmpty(),
    loyaltyId = loyaltyId.orEmpty(),
    method = kotlin.runCatching { CustomerCredentialMethod.valueOf(method.orEmpty().trim().uppercase()) }.getOrElse { CustomerCredentialMethod.BARCODE_IMAGE },
    status = kotlin.runCatching { CustomerCredentialStatus.valueOf(status.orEmpty().trim().uppercase()) }.getOrElse { CustomerCredentialStatus.AVAILABLE },
    referenceValue = referenceValue,
    updatedAt = (issuedAt ?: revokedAt)?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)

private fun CustomerBonusActionViewDto.toBonusAction() = CustomerBonusAction(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    storeId = storeId,
    type = kotlin.runCatching { CustomerBonusActionType.valueOf(type.orEmpty().trim().uppercase()) }.getOrElse { CustomerBonusActionType.OTHER },
    title = title.orEmpty(),
    details = details.orEmpty(),
    createdAt = createdAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)

private fun PointsLedgerEntryDto.toPointsLedger() = PointsLedger(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    transactionId = transactionId,
    delta = pointsDelta ?: 0,
    reason = reasonCode.orEmpty(),
    createdAt = createdAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)
