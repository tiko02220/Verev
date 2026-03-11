package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.LoyaltyTier

data class CustomerListUiState(
    val dataState: UiState<List<Customer>> = UiState.Loading,
    val searchQuery: String = "",
    val selectedTier: LoyaltyTier? = null,
    val filteredCustomers: List<Customer> = emptyList(),
) {
    val totalCustomers: Int
        get() = (dataState as? UiState.Success)?.data?.size ?: 0
}
