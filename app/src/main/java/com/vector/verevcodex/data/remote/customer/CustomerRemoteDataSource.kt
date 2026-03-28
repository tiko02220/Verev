package com.vector.verevcodex.data.remote.customer

import com.vector.verevcodex.common.phone.normalizePhoneNumber
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
    suspend fun list(storeId: String?, limit: Int = 100): Result<List<Customer>> = remoteResult {
        api.list(storeId = storeId, limit = limit).unwrap { list -> list.map { it.toCustomer() } }
    }

    suspend fun get(customerId: String): Result<Customer?> = remoteResult {
        api.get(customerId).unwrapNullable { it.toCustomer() }
    }

    suspend fun getDetail(customerId: String): Result<CustomerDetailResponseDto?> = remoteResult {
        api.get(customerId).unwrapNullable { it }
    }

    suspend fun create(draft: CustomerDraft, storeId: String): Result<Customer> = remoteResult {
        val normalizedPhoneNumber = normalizePhoneNumber(draft.phoneNumber)
        val request = CreateCustomerRequestDto(
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            phoneNumber = normalizedPhoneNumber,
            email = draft.email.trim().takeIf { it.isNotBlank() },
            gender = draft.gender?.name,
            birthDate = null,
            homeStoreId = storeId,
            tags = emptyList(),
            notes = null
        )
        api.create(request, customerIdempotencyKey(RemoteIdempotencyAction.CREATE, storeId, normalizedPhoneNumber))
            .unwrap { it.toCustomer() }
    }

    suspend fun quickRegister(firstName: String, phoneNumber: String, storeId: String): Result<Customer> = remoteResult {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val request = QuickRegisterCustomerRequestDto(
            firstName = firstName,
            lastName = "",
            phoneNumber = normalizedPhoneNumber,
            email = null,
            gender = null,
            birthDate = null,
            homeStoreId = storeId
        )
        api.quickRegister(request, customerIdempotencyKey(RemoteIdempotencyAction.QUICK_REGISTER, storeId, normalizedPhoneNumber))
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
    ): Result<Customer> = remoteResult {
        val detail = getDetail(customerId).getOrNull()
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val request = UpdateCustomerRequestDto(
            firstName = firstName,
            lastName = lastName,
            phoneNumber = normalizedPhoneNumber,
            email = email.takeIf { it.isNotBlank() },
            gender = detail?.customer?.gender,
            birthDate = detail?.customer?.birthDate,
            homeStoreId = homeStoreId,
            version = version,
        )
        api.update(
            customerId = customerId,
            request = request,
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.UPDATE, customerId, version.toString()),
        ).unwrap { it.toCustomer() }
    }

    suspend fun memberships(customerId: String): Result<List<CustomerBusinessRelation>> = remoteResult {
        api.memberships(customerId).unwrap { list -> list.map { it.toRelation() } }
    }

    suspend fun upsertMembership(customerId: String, storeId: String, notes: String, tags: List<String>): Result<CustomerBusinessRelation> = remoteResult {
        api.upsertMembership(customerId, storeId, UpdateMembershipRequestDto(notes, tags), customerIdempotencyKey(RemoteIdempotencyAction.UPSERT, customerId, storeId))
            .unwrap { it.toRelation() }
    }

    suspend fun credentials(customerId: String): Result<List<CustomerCredential>> = remoteResult {
        api.credentials(customerId).unwrap { list -> list.map { it.toCredential() } }
    }

    suspend fun createCredential(customerId: String, method: CustomerCredentialMethod, referenceValue: String?): Result<CustomerCredential> = remoteResult {
        api.createCredential(customerId, CreateCustomerCredentialRequestDto(method.name, referenceValue), customerIdempotencyKey(RemoteIdempotencyAction.CREATE, customerId, method.name))
            .unwrap { it.toCredential() }
    }

    suspend fun patchCredential(customerId: String, credentialId: String, status: CustomerCredentialStatus, referenceValue: String?): Result<CustomerCredential> = remoteResult {
        api.patchCredential(customerId, credentialId, PatchCustomerCredentialRequestDto(status.name, referenceValue), customerIdempotencyKey(RemoteIdempotencyAction.PATCH, credentialId, status.name))
            .unwrap { it.toCredential() }
    }

    suspend fun bonusActions(customerId: String): Result<List<CustomerBonusAction>> = remoteResult {
        api.bonusActions(customerId).unwrap { list -> list.map { it.toBonusAction() } }
    }

    suspend fun createBonusAction(
        customerId: String,
        storeId: String?,
        type: CustomerBonusActionType,
        title: String,
        details: String,
    ): Result<CustomerBonusAction> = remoteResult {
        api.createBonusAction(
            customerId = customerId,
            request = CreateCustomerBonusActionRequestDto(storeId = storeId, type = type.name, title = title, details = details),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.CREATE, customerId, type.name, title),
        ).unwrap { it.toBonusAction() }
    }

    suspend fun pointsLedger(customerId: String, limit: Int = 100): Result<List<PointsLedger>> = remoteResult {
        api.pointsLedger(customerId, limit).unwrap { list -> list.map { it.toPointsLedger() } }
    }

    suspend fun adjustPoints(customerId: String, storeId: String, delta: Int, reason: String): Result<CustomerAdjustmentResult> = remoteResult {
        api.adjustPoints(
            customerId = customerId,
            request = ManualPointsAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.ADJUST_POINTS, customerId, storeId, delta.toString()),
        ).unwrap { response ->
            CustomerAdjustmentResult(
                approvalRequired = response.approvalRequired == true || response.approvalRequest != null,
            )
        }
    }

    suspend fun adjustVisits(customerId: String, storeId: String, delta: Int, reason: String): Result<CustomerAdjustmentResult> = remoteResult {
        api.adjustVisits(
            customerId = customerId,
            request = ManualVisitAdjustmentRequestDto(storeId = storeId, delta = delta, reason = reason),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.ADJUST_VISITS, customerId, storeId, delta.toString()),
        ).unwrap { CustomerAdjustmentResult() }
    }

    suspend fun findByLoyaltyId(loyaltyId: String, storeId: String? = null): Result<Customer?> = remoteResult {
        api.byLoyaltyId(loyaltyId, storeId).unwrapNullable { it.toCustomer() }
    }

    suspend fun previewMerge(sourceCustomerId: String, targetCustomerId: String): Result<CustomerMergePreview> = remoteResult {
        api.mergePreview(sourceCustomerId, targetCustomerId).unwrap { dto ->
            CustomerMergePreview(
                sourceCustomer = requireRemoteCustomer(dto.sourceCustomer),
                targetCustomer = requireRemoteCustomer(dto.targetCustomer),
                assessment = CustomerMergeAssessment(
                    sourceOrganizationProfiles = dto.assessment?.sourceOrganizationProfiles.orZero(),
                    overlappingOrganizationProfiles = dto.assessment?.overlappingOrganizationProfiles.orZero(),
                    sourceMemberships = dto.assessment?.sourceMemberships.orZero(),
                    overlappingMemberships = dto.assessment?.overlappingMemberships.orZero(),
                    sourceCredentials = dto.assessment?.sourceCredentials.orZero(),
                    overlappingCredentials = dto.assessment?.overlappingCredentials.orZero(),
                    sourceBonusActions = dto.assessment?.sourceBonusActions.orZero(),
                    sourceTransactions = dto.assessment?.sourceTransactions.orZero(),
                    sourcePointsLedgerEntries = dto.assessment?.sourcePointsLedgerEntries.orZero(),
                ),
                warnings = dto.warnings.orEmpty(),
                blockingReasons = dto.blockingReasons.orEmpty(),
                canMerge = dto.canMerge.orFalse(),
            )
        }
    }

    suspend fun mergeCustomers(sourceCustomerId: String, targetCustomerId: String, notes: String?): Result<CustomerMergeResult> = remoteResult {
        api.merge(
            request = MergeCustomersRequestDto(
                sourceCustomerId = sourceCustomerId,
                targetCustomerId = targetCustomerId,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
            ),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.UPDATE, sourceCustomerId, targetCustomerId, "merge"),
        ).unwrap { dto ->
            CustomerMergeResult(
                sourceCustomerId = dto.sourceCustomerId.orEmpty(),
                targetCustomer = requireRemoteValue(dto.targetCustomer, "Missing merged customer payload").toCustomer(),
            )
        }
    }

    suspend fun previewSplit(sourceCustomerId: String, organizationId: String): Result<CustomerSplitPreview> = remoteResult {
        api.splitPreview(sourceCustomerId, organizationId).unwrap { dto ->
            CustomerSplitPreview(
                sourceCustomer = requireRemoteCustomer(dto.sourceCustomer),
                organizationId = dto.organizationId.orEmpty(),
                assessment = CustomerSplitAssessment(
                    organizationProfilesToMove = dto.assessment?.organizationProfilesToMove.orZero(),
                    membershipsToMove = dto.assessment?.membershipsToMove.orZero(),
                    credentialsToMove = dto.assessment?.credentialsToMove.orZero(),
                    bonusActionsToMove = dto.assessment?.bonusActionsToMove.orZero(),
                    transactionsToMove = dto.assessment?.transactionsToMove.orZero(),
                    approvalRequestsToMove = dto.assessment?.approvalRequestsToMove.orZero(),
                    pointsLedgerEntriesToMove = dto.assessment?.pointsLedgerEntriesToMove.orZero(),
                    remainingOrganizationProfiles = dto.assessment?.remainingOrganizationProfiles.orZero(),
                    sourceHasConsumerAccount = dto.assessment?.sourceHasConsumerAccount.orFalse(),
                ),
                warnings = dto.warnings.orEmpty(),
                blockingReasons = dto.blockingReasons.orEmpty(),
                canSplit = dto.canSplit.orFalse(),
            )
        }
    }

    suspend fun splitCustomer(
        sourceCustomerId: String,
        organizationId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        notes: String?,
    ): Result<CustomerSplitResult> = remoteResult {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        api.split(
            request = SplitCustomerRequestDto(
                sourceCustomerId = sourceCustomerId,
                organizationId = organizationId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = normalizedPhoneNumber,
                email = email.trim().takeIf { it.isNotBlank() },
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
            ),
            idempotencyKey = customerIdempotencyKey(RemoteIdempotencyAction.UPDATE, sourceCustomerId, organizationId, normalizedPhoneNumber, "split"),
        ).unwrap { dto ->
            CustomerSplitResult(
                sourceCustomerId = dto.sourceCustomerId.orEmpty(),
                targetCustomer = requireRemoteValue(dto.targetCustomer, "Missing split customer payload").toCustomer(),
            )
        }
    }

    private fun customerIdempotencyKey(action: RemoteIdempotencyAction, vararg parts: String?): String = 
        buildRemoteIdempotencyKey(domain = RemoteIdempotencyDomain.CUSTOMER, action = action, *parts)

    private fun requireRemoteCustomer(customer: CustomerViewDto?): Customer =
        CustomerDetailResponseDto(
            customer = customer,
            profile = CustomerOrganizationProfileViewDto(),
            memberships = emptyList(),
            credentials = emptyList(),
        ).toCustomer()
}
