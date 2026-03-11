package com.vector.verevcodex.domain.model

data class CustomerDraft(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val loyaltyId: String? = null,
)
