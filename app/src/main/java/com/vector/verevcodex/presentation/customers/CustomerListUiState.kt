package com.vector.verevcodex.presentation.customers

import androidx.annotation.StringRes
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.domain.model.common.LoyaltyTier

internal data class CustomerListUiState(
    val dataState: UiState<List<CustomerListCardUi>> = UiState.Loading,
    @StringRes val errorRes: Int? = null,
    val searchQuery: String = "",
    val selectedTier: LoyaltyTier? = null,
    val selectedStoreName: String = "",
    val filteredCustomers: List<CustomerListCardUi> = emptyList(),
) {
    val totalCustomers: Int
        get() = (dataState as? UiState.Success)?.data?.size ?: 0
}
