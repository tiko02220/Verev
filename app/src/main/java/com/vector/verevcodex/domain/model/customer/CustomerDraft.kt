package com.vector.verevcodex.domain.model.customer

import java.time.LocalDate

data class CustomerDraft(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val loyaltyId: String? = null,
    val gender: CustomerGender? = null,
    val birthDate: LocalDate? = null,
)
