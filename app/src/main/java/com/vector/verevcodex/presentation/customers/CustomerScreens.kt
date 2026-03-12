package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.displayName

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
                storeName = state.selectedStoreName,
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
                                subtitle = state.errorRes?.let { stringResource(it) }
                                    ?: dataState.message.ifBlank { stringResource(R.string.merchant_customers_error_subtitle) },
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
                            items(state.filteredCustomers, key = { it.customer.id }) { customer ->
                                CustomerCard(
                                    customer = customer,
                                    onOpenProfile = { onOpenCustomer(customer.customer.id) },
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
    onOpenTransaction: (String) -> Unit = {},
    onManageCredentials: (String) -> Unit = {},
    viewModel: CustomerProfileViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val customer = state.customer
    var showEditContact by rememberSaveable { mutableStateOf(false) }
    var showEditCrm by rememberSaveable { mutableStateOf(false) }
    var showAdjustPoints by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(CustomerProfileTab.OVERVIEW) }

    if (showEditContact && customer != null) {
        EditCustomerContactDialog(
            customer = customer,
            isSaving = state.isSaving,
            onDismiss = { showEditContact = false },
            onSave = { firstName, lastName, phone, email ->
                viewModel.updateContact(firstName, lastName, phone, email)
                showEditContact = false
            },
        )
    }

    if (showEditCrm) {
        EditCustomerCrmDialog(
            relation = state.relation,
            isSaving = state.isSaving,
            onDismiss = { showEditCrm = false },
            onSave = { notes, tags ->
                viewModel.updateNotesAndTags(notes, tags)
                showEditCrm = false
            },
        )
    }

    if (showAdjustPoints) {
        AdjustCustomerPointsDialog(
            isSaving = state.isSaving,
            onDismiss = { showAdjustPoints = false },
            onSave = { delta, reason ->
                viewModel.adjustPoints(delta, reason)
                showAdjustPoints = false
            },
        )
    }

    LaunchedEffect(state.feedbackMessageRes) {
        if (state.feedbackMessageRes != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.consumeFeedback()
        }
    }

    CustomerFeatureScaffold(
        title = customer?.displayName().orEmpty().ifBlank { stringResource(R.string.merchant_customer_profile) },
        subtitle = customer?.email.orEmpty().ifBlank { stringResource(R.string.merchant_customer_profile_missing_subtitle) },
        onBack = onBack,
        headerContent = {
            customer?.let {
                CustomerProfileHero(customer = it, relation = state.relation)
            }
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
                        state.feedbackMessageRes?.let { messageRes ->
                            item {
                                CustomerBodySection {
                                    androidx.compose.material3.Text(
                                        text = stringResource(messageRes),
                                        color = com.vector.verevcodex.presentation.theme.VerevColors.Forest,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                        item { CustomerProfileTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
                        when (selectedTab) {
                            CustomerProfileTab.OVERVIEW -> {
                                item { CustomerProfileTierProgressSection(customer = customer, transactions = state.transactions) }
                                item { CustomerProfileContactSection(customer = customer, onEditContact = { showEditContact = true }) }
                                if (state.transactions.isEmpty()) {
                                    item {
                                        MerchantEmptyStateCard(
                                            title = stringResource(R.string.merchant_customer_transactions_empty_title),
                                            subtitle = stringResource(R.string.merchant_customer_transactions_empty_subtitle),
                                            icon = Icons.Default.CreditCard,
                                        )
                                    }
                                } else {
                                    item { CustomerProfileTransactionSection(state.transactions, onOpenTransaction) }
                                }
                            }
                            CustomerProfileTab.CRM -> {
                                item {
                                    CustomerCrmSection(
                                        relation = state.relation,
                                        onEditCrm = { showEditCrm = true },
                                    )
                                }
                            }
                            CustomerProfileTab.BONUSES -> {
                                item {
                                    CustomerBonusManagementSection(
                                        customer = customer,
                                        relation = state.relation,
                                        ledgerEntries = state.ledgerEntries,
                                        rewards = state.credentials,
                                        onAdjustPoints = { showAdjustPoints = true },
                                    )
                                }
                            }
                            CustomerProfileTab.ACTIVITY -> {
                                if (state.activities.isNotEmpty()) {
                                    item { CustomerActivitySection(state.activities, onOpenTransaction) }
                                }
                            }
                            CustomerProfileTab.ACCESS -> {
                                if (state.credentials.isNotEmpty()) {
                                    item {
                                        CustomerProfileCredentialSection(
                                            credentials = state.credentials,
                                            onManageCredentials = { onManageCredentials(customer.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun CustomerTransactionDetailScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: CustomerTransactionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val transaction = state.transaction
    CustomerFeatureScaffold(
        title = stringResource(R.string.merchant_customer_transaction_detail_title),
        subtitle = transaction?.metadata?.ifBlank { stringResource(R.string.merchant_transaction_item_fallback) }
            ?: stringResource(R.string.merchant_customer_transactions_empty_subtitle),
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
                    state.isMissingTransaction -> item {
                        MerchantEmptyStateCard(
                            title = stringResource(R.string.merchant_customer_transaction_missing_title),
                            subtitle = stringResource(R.string.merchant_customer_transaction_missing_subtitle),
                            icon = Icons.Default.CreditCard,
                        )
                    }
                    transaction == null -> item {
                        MerchantEmptyStateCard(
                            title = stringResource(R.string.merchant_customers_loading_title),
                            subtitle = stringResource(R.string.merchant_customers_loading_subtitle),
                            icon = Icons.Default.CreditCard,
                        )
                    }
                    else -> item { CustomerTransactionDetailSection(transaction) }
                }
            }
        },
    )
}
