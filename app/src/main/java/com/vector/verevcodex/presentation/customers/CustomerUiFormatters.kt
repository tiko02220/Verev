package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.Customer

internal fun Customer.displayName(): String =
    listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { email.ifBlank { loyaltyId } }
