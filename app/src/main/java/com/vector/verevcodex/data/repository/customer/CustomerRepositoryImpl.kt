package com.vector.verevcodex.data.repository.customer

import com.vector.verevcodex.common.identifiers.LoyaltyIdCodec
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.entity.customer.CustomerBonusActionEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerCredentialEntity
import com.vector.verevcodex.data.db.entity.customer.CustomerEntity
import com.vector.verevcodex.data.db.entity.loyalty.PointsLedgerEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerDraft
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import com.vector.verevcodex.domain.repository.customer.CustomerRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : CustomerRepository {
    override fun observeCustomers(storeId: String?): Flow<List<Customer>> =
        database.customerDao().observeCustomers(storeId).map { list -> list.distinctBy { it.id }.map { it.toDomain() } }

    override fun observeCustomerRelationsByStore(storeId: String): Flow<List<CustomerBusinessRelation>> =
        database.customerBusinessRelationDao().observeRelationsByStore(storeId).map { relations ->
            relations.map { it.toDomain() }
        }

    override fun observeCustomer(customerId: String): Flow<Customer?> =
        database.customerDao().observeCustomer(customerId).map { it?.toDomain() }

    override fun observeCustomerRelations(customerId: String): Flow<List<CustomerBusinessRelation>> =
        database.customerBusinessRelationDao().observeRelations(customerId).map { relations -> relations.map { it.toDomain() } }

    override fun observeCustomerCredentials(customerId: String): Flow<List<CustomerCredential>> =
        database.customerCredentialDao().observeCredentials(customerId).map { list -> list.map { it.toDomain() } }

    override fun observeCustomerPointsLedger(customerId: String): Flow<List<PointsLedger>> =
        database.pointsLedgerDao().observeByCustomerId(customerId).map { ledger -> ledger.map { it.toDomain() } }

    override fun observeCustomerBonusActions(customerId: String): Flow<List<CustomerBonusAction>> =
        database.customerBonusActionDao().observeActions(customerId).map { actions -> actions.map { it.toDomain() } }

    override suspend fun findByLoyaltyId(loyaltyId: String): Customer? =
        database.customerDao().findByLoyaltyId(loyaltyId)?.toDomain()

    override suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            phoneNumber = draft.phoneNumber.trim(),
            email = draft.email.trim().lowercase(),
            loyaltyId = draft.loyaltyId?.trim()?.ifBlank { null } ?: LoyaltyIdCodec.generate(),
            enrolledDate = LocalDate.now().toString(),
            totalVisits = 0,
            totalSpent = 0.0,
            currentPoints = 0,
            loyaltyTier = LoyaltyTier.BRONZE.name,
            lastVisit = null,
            favoriteStoreId = storeId,
        )
        database.customerDao().insert(customer)
        database.customerBusinessRelationDao().insert(
            CustomerBusinessRelationEntity(
                id = UUID.randomUUID().toString(),
                customerId = customer.id,
                storeId = storeId,
                joinedAt = LocalDateTime.now().toString(),
                notes = "Manual registration from dashboard flow",
                tags = "",
            )
        )
        database.customerCredentialDao().insertAll(
            listOf(
                CustomerCredentialEntity(
                    id = UUID.randomUUID().toString(),
                    customerId = customer.id,
                    loyaltyId = customer.loyaltyId,
                    method = CustomerCredentialMethod.BARCODE_IMAGE.name,
                    status = CustomerCredentialStatus.LINKED.name,
                    referenceValue = customer.loyaltyId,
                    updatedAt = LocalDateTime.now().toString(),
                ),
                CustomerCredentialEntity(
                    id = UUID.randomUUID().toString(),
                    customerId = customer.id,
                    loyaltyId = customer.loyaltyId,
                    method = CustomerCredentialMethod.GOOGLE_WALLET.name,
                    status = CustomerCredentialStatus.AVAILABLE.name,
                    referenceValue = null,
                    updatedAt = LocalDateTime.now().toString(),
                ),
                CustomerCredentialEntity(
                    id = UUID.randomUUID().toString(),
                    customerId = customer.id,
                    loyaltyId = customer.loyaltyId,
                    method = CustomerCredentialMethod.NFC_CARD.name,
                    status = CustomerCredentialStatus.AVAILABLE.name,
                    referenceValue = null,
                    updatedAt = LocalDateTime.now().toString(),
                ),
            )
        )
        return customer.toDomain()
    }

    override suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, loyaltyId: String, storeId: String): Customer {
        return createCustomer(
            draft = CustomerDraft(
                firstName = firstName,
                lastName = "",
                phoneNumber = phoneNumber,
                email = "",
                loyaltyId = loyaltyId,
            ),
            storeId = storeId,
        )
    }

    override suspend fun updateCustomerContact(
        customerId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        favoriteStoreId: String?,
    ) {
        val existing = database.customerDao().getCustomer(customerId) ?: return
        database.customerDao().update(
            existing.copy(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phoneNumber = phoneNumber.trim(),
                email = email.trim().lowercase(),
                favoriteStoreId = favoriteStoreId,
            )
        )
    }

    override suspend fun updateCustomerNotesAndTags(
        customerId: String,
        storeId: String,
        notes: String,
        tags: List<String>,
    ) {
        val relation = database.customerBusinessRelationDao().getRelation(customerId, storeId)
            ?: CustomerBusinessRelationEntity(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                storeId = storeId,
                joinedAt = LocalDateTime.now().toString(),
                notes = "",
                tags = "",
            )
        database.customerBusinessRelationDao().insert(
            relation.copy(
                notes = notes.trim(),
                tags = tags.joinToString(",") { it.trim() },
            )
        )
        database.pointsLedgerDao().insert(
            PointsLedgerEntity(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                transactionId = null,
                delta = 0,
                reason = "Customer CRM details updated",
                createdAt = LocalDateTime.now().toString(),
            )
        )
    }

    override suspend fun upsertCustomerCredential(
        customerId: String,
        loyaltyId: String,
        method: CustomerCredentialMethod,
        status: CustomerCredentialStatus,
        referenceValue: String?,
    ) {
        val existing = database.customerCredentialDao().getCredential(customerId, method.name)
        database.customerCredentialDao().insert(
            CustomerCredentialEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                customerId = customerId,
                loyaltyId = loyaltyId,
                method = method.name,
                status = status.name,
                referenceValue = referenceValue,
                updatedAt = LocalDateTime.now().toString(),
            )
        )
    }

    override suspend fun adjustPoints(customerId: String, delta: Int, reason: String) {
        val customer = database.customerDao().getCustomer(customerId) ?: return
        database.customerDao().update(customer.copy(currentPoints = customer.currentPoints + delta))
        database.pointsLedgerDao().insert(
            PointsLedgerEntity(UUID.randomUUID().toString(), customerId, null, delta, reason, LocalDateTime.now().toString())
        )
    }

    override suspend fun recordBonusAction(
        customerId: String,
        storeId: String?,
        type: CustomerBonusActionType,
        title: String,
        details: String,
    ) {
        database.customerBonusActionDao().insert(
            CustomerBonusActionEntity(
                id = UUID.randomUUID().toString(),
                customerId = customerId,
                storeId = storeId,
                type = type.name,
                title = title,
                details = details,
                createdAt = LocalDateTime.now().toString(),
            )
        )
    }
}
