package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.customer.Customer

data class CustomerListCardUi(
    val customer: Customer,
    val showsTierBadge: Boolean = true,
    val notesPreview: String? = null,
    val tagsPreview: List<String> = emptyList(),
)
