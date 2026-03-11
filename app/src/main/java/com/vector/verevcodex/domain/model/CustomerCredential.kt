package com.vector.verevcodex.domain.model

import java.time.LocalDateTime

data class CustomerCredential(
    val customerId: String,
    val loyaltyId: String,
    val method: CustomerCredentialMethod,
    val status: CustomerCredentialStatus,
    val referenceValue: String?,
    val updatedAt: LocalDateTime,
)
