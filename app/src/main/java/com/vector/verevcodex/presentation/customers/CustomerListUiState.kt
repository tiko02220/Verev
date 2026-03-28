package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.domain.model.common.LoyaltyTier

data class CustomerListUiState(
    val dataState: UiState<List<CustomerListCardUi>> = UiState.Loading,
    @StringRes val errorRes: Int? = null,
    val searchQuery: String = "",
    val selectedTier: LoyaltyTier? = null,
    val selectedSort: CustomerListSortOption = CustomerListSortOption.RECENT_ACTIVITY,
    val selectedStoreName: String = "",
    val hasActiveTierProgram: Boolean = false,
    val filteredCustomers: List<CustomerListCardUi> = emptyList(),
) {
    val totalCustomers: Int
        get() = (dataState as? UiState.Success)?.data?.size ?: 0

    val filteredVisits: Int
        get() = filteredCustomers.sumOf { it.customer.totalVisits }

    val filteredRevenue: Double
        get() = filteredCustomers.sumOf { it.customer.totalSpent }
}

enum class CustomerListSortOption(@StringRes val labelRes: Int) {
    RECENT_ACTIVITY(R.string.merchant_customers_sort_recent),
    HIGHEST_SPEND(R.string.merchant_customers_sort_spend),
    HIGHEST_POINTS(R.string.merchant_customers_sort_points),
    NAME(R.string.merchant_customers_sort_name),
}
