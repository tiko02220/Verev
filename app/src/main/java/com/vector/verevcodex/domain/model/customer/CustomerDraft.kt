package com.vector.verevcodex.domain.model.customer

data class CustomerDraft(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val loyaltyId: String? = null,
    val gender: CustomerGender? = null,
)
