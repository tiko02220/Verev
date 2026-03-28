package com.vector.verevcodex.data.mapper

import com.vector.verevcodex.data.remote.api.customer.CustomerBonusActionViewDto
import com.vector.verevcodex.data.remote.api.customer.CustomerCredentialViewDto
import com.vector.verevcodex.data.remote.api.customer.CustomerDetailResponseDto
import com.vector.verevcodex.data.remote.api.customer.CustomerMembershipViewDto
import com.vector.verevcodex.data.remote.api.customer.PointsLedgerEntryDto
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerBonusAction
import com.vector.verevcodex.domain.model.customer.CustomerBonusActionType
import com.vector.verevcodex.domain.model.customer.CustomerBusinessRelation
import com.vector.verevcodex.domain.model.customer.CustomerCredential
import com.vector.verevcodex.domain.model.customer.CustomerCredentialMethod
import com.vector.verevcodex.domain.model.customer.CustomerCredentialStatus
import com.vector.verevcodex.domain.model.customer.CustomerGender
import com.vector.verevcodex.domain.model.loyalty.PointsLedger
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

fun CustomerDetailResponseDto.toCustomer() = Customer(
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
    loyaltyTier = runCatching { LoyaltyTier.valueOf(profile?.loyaltyTier.orEmpty().trim().uppercase()) }.getOrElse { LoyaltyTier.BRONZE },
    loyaltyTierLabel = profile?.loyaltyTier?.trim().orEmpty(),
    lastVisit = null,
    favoriteStoreId = profile?.homeStoreId,
    gender = customer?.gender?.let { raw ->
        runCatching { CustomerGender.valueOf(raw.trim().uppercase()) }.getOrNull()
    },
)

fun CustomerMembershipViewDto.toRelation() = CustomerBusinessRelation(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    storeId = storeId.orEmpty(),
    joinedAt = joinedAt?.let { runCatching { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) }.getOrNull() } ?: LocalDateTime.now(),
    notes = notes.orEmpty(),
    tags = tags.orEmpty(),
)

fun CustomerCredentialViewDto.toCredential() = CustomerCredential(
    customerId = customerId.orEmpty(),
    loyaltyId = loyaltyId.orEmpty(),
    method = runCatching { CustomerCredentialMethod.valueOf(method.orEmpty().trim().uppercase()) }.getOrElse { CustomerCredentialMethod.BARCODE_IMAGE },
    status = runCatching { CustomerCredentialStatus.valueOf(status.orEmpty().trim().uppercase()) }.getOrElse { CustomerCredentialStatus.AVAILABLE },
    referenceValue = referenceValue,
    updatedAt = (issuedAt ?: revokedAt)?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)

fun CustomerBonusActionViewDto.toBonusAction() = CustomerBonusAction(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    storeId = storeId,
    type = runCatching { CustomerBonusActionType.valueOf(type.orEmpty().trim().uppercase()) }.getOrElse { CustomerBonusActionType.OTHER },
    title = title.orEmpty(),
    details = details.orEmpty(),
    createdAt = createdAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)

fun PointsLedgerEntryDto.toPointsLedger() = PointsLedger(
    id = id.orEmpty(),
    customerId = customerId.orEmpty(),
    transactionId = transactionId,
    delta = pointsDelta ?: 0,
    reason = reasonCode.orEmpty(),
    createdAt = createdAt?.let { LocalDateTime.ofInstant(Instant.parse(it), ZoneOffset.UTC) } ?: LocalDateTime.now(),
)
