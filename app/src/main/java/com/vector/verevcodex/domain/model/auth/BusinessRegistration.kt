package com.vector.verevcodex.domain.model.auth

data class BusinessRegistration(
    val businessName: String,
    val industry: String,
    val address: String,
    val city: String,
    val zipCode: String,
    val phoneNumber: String,
    val businessEmail: String,
)
