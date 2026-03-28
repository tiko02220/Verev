package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.presentation.common.state.UiState
import java.time.LocalDateTime

internal fun filterCustomers(
    dataState: UiState<List<CustomerListCardUi>>,
    query: String,
    tier: LoyaltyTier?,
    sort: CustomerListSortOption,
) : List<CustomerListCardUi> {
    val customers = (dataState as? UiState.Success)?.data.orEmpty()
    return customers
        .filter { item ->
        val customer = item.customer
        val fullName = customer.fullName()
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
        .sortedWith(sort.comparator())
}

private fun CustomerListSortOption.comparator(): Comparator<CustomerListCardUi> = when (this) {
    CustomerListSortOption.RECENT_ACTIVITY -> compareByDescending<CustomerListCardUi> {
        it.customer.lastVisit ?: it.customer.enrolledDate.atStartOfDay()
    }.thenBy { it.customer.fullName().lowercase() }
    CustomerListSortOption.HIGHEST_SPEND -> compareByDescending<CustomerListCardUi> {
        it.customer.totalSpent
    }.thenByDescending { it.customer.totalVisits }
        .thenBy { it.customer.fullName().lowercase() }
    CustomerListSortOption.HIGHEST_POINTS -> compareByDescending<CustomerListCardUi> {
        it.customer.currentPoints
    }.thenByDescending { it.customer.totalVisits }
        .thenBy { it.customer.fullName().lowercase() }
    CustomerListSortOption.NAME -> compareBy<CustomerListCardUi> {
        it.customer.fullName().lowercase()
    }.thenByDescending { it.customer.lastVisit ?: LocalDateTime.MIN }
}

private fun com.vector.verevcodex.domain.model.customer.Customer.fullName(): String =
    listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { loyaltyId }
