package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard

@Composable
fun CustomerListScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomer: (String) -> Unit = {},
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTier by rememberSaveable { mutableStateOf<LoyaltyTier?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val customerState = state) {
            UiState.Loading -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customers_loading_title),
                        subtitle = stringResource(R.string.merchant_customers_loading_subtitle),
                        icon = Icons.Default.Person,
                    )
                }
            }
            UiState.Empty -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customers_empty_title),
                        subtitle = stringResource(R.string.merchant_customers_empty_subtitle),
                        icon = Icons.Default.Person,
                    )
                }
            }
            is UiState.Error -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customers_error_title),
                        subtitle = customerState.message,
                        icon = Icons.Default.Person,
                    )
                }
            }
            is UiState.Success -> {
                val filteredCustomers = customerState.data.filter { customer ->
                    val fullName = listOf(customer.firstName, customer.lastName).joinToString(" ").trim()
                    val matchesSearch = searchQuery.isBlank() ||
                        fullName.contains(searchQuery, ignoreCase = true) ||
                        customer.email.contains(searchQuery, ignoreCase = true)
                    val matchesTier = selectedTier == null || customer.loyaltyTier == selectedTier
                    matchesSearch && matchesTier
                }
                item { CustomersHeader(totalCount = customerState.data.size) }
                item { CustomerSearchBar(query = searchQuery, onQueryChange = { searchQuery = it }) }
                item { CustomerTierFilters(selectedTier = selectedTier, onTierSelected = { selectedTier = it }) }
                if (filteredCustomers.isEmpty()) {
                    item {
                        MerchantEmptyStateCard(
                            title = stringResource(R.string.merchant_customers_filtered_empty_title),
                            subtitle = stringResource(R.string.merchant_customers_filtered_empty_subtitle),
                            icon = Icons.Default.Person,
                        )
                    }
                } else {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onRewardBoost = { viewModel.rewardLoyaltyBoost(customer.id) },
                            onOpenProfile = { onOpenCustomer(customer.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerProfileScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: CustomerProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val customer = state.customer
        when {
            state.isMissingCustomer -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customer_profile_missing_title),
                        subtitle = stringResource(R.string.merchant_customer_profile_missing_subtitle),
                        icon = Icons.Default.Person,
                    )
                }
            }
            customer == null -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_customers_loading_title),
                        subtitle = stringResource(R.string.merchant_customers_loading_subtitle),
                        icon = Icons.Default.Person,
                    )
                }
            }
            else -> {
                item { CustomerProfileHeader(customer = customer, onBack = onBack) }
                item { CustomerProfileSummaryCard(customer = customer) }
                item { CustomerProfileContactCard(customer = customer) }
                if (state.transactions.isEmpty()) {
                    item {
                        MerchantEmptyStateCard(
                            title = stringResource(R.string.merchant_customer_transactions_empty_title),
                            subtitle = stringResource(R.string.merchant_customer_transactions_empty_subtitle),
                            icon = Icons.Default.Person,
                        )
                    }
                } else {
                    item { CustomerProfileTransactions(state.transactions) }
                }
            }
        }
    }
}
