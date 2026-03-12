package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.presentation.common.state.UiState

internal fun filterCustomers(
    dataState: UiState<List<CustomerListCardUi>>,
    query: String,
    tier: LoyaltyTier?,
) : List<CustomerListCardUi> {
    val customers = (dataState as? UiState.Success)?.data.orEmpty()
    return customers.filter { item ->
        val customer = item.customer
        val fullName = listOf(customer.firstName, customer.lastName).joinToString(" ").trim()
        val matchesSearch = query.isBlank() ||
            fullName.contains(query, ignoreCase = true) ||
            customer.email.contains(query, ignoreCase = true) ||
            customer.phoneNumber.contains(query, ignoreCase = true) ||
            customer.loyaltyId.contains(query, ignoreCase = true) ||
            item.notesPreview.orEmpty().contains(query, ignoreCase = true) ||
            item.tagsPreview.any { it.contains(query, ignoreCase = true) }
        val matchesTier = tier == null || customer.loyaltyTier == tier
        matchesSearch && matchesTier
    }
}
