package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.presentation.merchant.common.displayName
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard

@Composable
fun CustomerListScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomer: (String) -> Unit = {},
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    CustomerFeatureScaffold(
        title = stringResource(R.string.merchant_customers_title),
        subtitle = stringResource(R.string.merchant_customers_subtitle, state.totalCustomers),
        headerContent = {
            CustomersHeaderPanel(
                totalCount = state.totalCustomers,
                query = state.searchQuery,
                selectedTier = state.selectedTier,
                onQueryChange = viewModel::onSearchQueryChanged,
                onTierSelected = viewModel::onTierSelected,
            )
        },
        body = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 20.dp,
                    bottom = contentPadding.calculateBottomPadding() + 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when (val dataState = state.dataState) {
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
                                subtitle = dataState.message,
                                icon = Icons.Default.Person,
                            )
                        }
                    }
                    is UiState.Success -> {
                        if (state.filteredCustomers.isEmpty()) {
                            item {
                                MerchantEmptyStateCard(
                                    title = stringResource(R.string.merchant_customers_filtered_empty_title),
                                    subtitle = stringResource(R.string.merchant_customers_filtered_empty_subtitle),
                                    icon = Icons.Default.Person,
                                )
                            }
                        } else {
                            items(state.filteredCustomers, key = { it.id }) { customer ->
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
        },
    )
}

@Composable
fun CustomerProfileScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    onManageCredentials: (String) -> Unit = {},
    viewModel: CustomerProfileViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val customer = state.customer

    CustomerFeatureScaffold(
        title = customer?.displayName().orEmpty().ifBlank {
            stringResource(R.string.merchant_customer_profile)
        },
        subtitle = customer?.email.orEmpty().ifBlank {
            stringResource(R.string.merchant_customer_profile_missing_subtitle)
        },
        onBack = onBack,
        body = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 20.dp,
                    bottom = contentPadding.calculateBottomPadding() + 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                        item { CustomerProfileHero(customer = customer) }
                        if (state.credentials.isNotEmpty()) {
                            item {
                                CustomerProfileCredentialSection(
                                    credentials = state.credentials,
                                    onManageCredentials = { onManageCredentials(customer.id) },
                                )
                            }
                        }
                        item { CustomerProfileContactSection(customer = customer) }
                        if (state.transactions.isEmpty()) {
                            item {
                                MerchantEmptyStateCard(
                                    title = stringResource(R.string.merchant_customer_transactions_empty_title),
                                    subtitle = stringResource(R.string.merchant_customer_transactions_empty_subtitle),
                                    icon = Icons.Default.Person,
                                )
                            }
                        } else {
                            item { CustomerProfileTransactionSection(state.transactions) }
                        }
                    }
                }
            }
        },
    )
}
