package com.vector.verevcodex.presentation.customers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun CustomerListScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomer: (String) -> Unit = {},
    onOpenAddCustomer: () -> Unit = {},
    viewModel: CustomerViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }

    CustomerFeatureScaffold(
        title = stringResource(R.string.merchant_customers_title),
        subtitle = stringResource(R.string.merchant_customers_subtitle, state.totalCustomers),
        headerStyle = CustomerFeatureHeaderStyle.PLAIN,
        showTitle = false,
        headerContent = {
            CustomersHeaderPanel(
                title = stringResource(R.string.merchant_customers_title),
                subtitle = stringResource(R.string.merchant_customers_subtitle, state.totalCustomers),
                totalCount = state.filteredCustomers.size,
                totalVisits = state.filteredVisits,
                totalRevenue = state.filteredRevenue,
                query = state.searchQuery,
                selectedTier = state.selectedTier,
                selectedSort = state.selectedSort,
                hasActiveTierProgram = state.hasActiveTierProgram,
                onOpenAddCustomer = onOpenAddCustomer,
                onOpenFilter = { showFilterSheet = true },
                onOpenSort = { showSortSheet = true },
                onQueryChange = viewModel::onSearchQueryChanged,
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
                            CustomerListSkeleton()
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

    if (showFilterSheet) {
        CustomerTierFilterSheet(
            hasActiveTierProgram = state.hasActiveTierProgram,
            selectedTier = state.selectedTier,
            onDismiss = { showFilterSheet = false },
            onTierSelected = viewModel::onTierSelected,
        )
    }

    if (showSortSheet) {
        CustomerSortSheet(
            selectedSort = state.selectedSort,
            onDismiss = { showSortSheet = false },
            onSortSelected = viewModel::onSortSelected,
        )
    }
}

@Composable
fun CustomerProfileScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    onOpenCustomer: (String) -> Unit = {},
    onOpenTransaction: (String) -> Unit = {},
    onManageCredentials: (String) -> Unit = {},
    viewModel: CustomerProfileViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val customer = state.customer
    var showEditContact by rememberSaveable { mutableStateOf(false) }
    var showEditCrm by rememberSaveable { mutableStateOf(false) }
    var showAdjustPoints by rememberSaveable { mutableStateOf(false) }
    var showAddTransactionSheet by rememberSaveable { mutableStateOf(false) }
    var showTagsSheet by rememberSaveable { mutableStateOf(false) }
    var showMergeSheet by rememberSaveable { mutableStateOf(false) }
    var showSplitSheet by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(CustomerProfileTab.OVERVIEW) }
    val hasActiveTierProgram = state.storePrograms.any { it.active && it.configuration.tierTrackingEnabled }
    val availablePointsPrograms = state.storePrograms.eligibleActivePrograms()

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
        CustomerAdjustPointsSheet(
            currentPoints = customer?.currentPoints ?: 0,
            currentVisits = state.scopedVisits,
            availablePrograms = availablePointsPrograms,
            isSaving = state.isSaving,
            onDismiss = { showAdjustPoints = false },
            onSave = { delta, reason, programId ->
                viewModel.adjustPoints(delta, reason, programId)
                showAdjustPoints = false
            },
        )
    }

    if (showAddTransactionSheet && customer != null) {
        CustomerTransactionSheet(
            availablePrograms = availablePointsPrograms,
            isSaving = state.isSaving,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { amount, description, programId ->
                viewModel.recordManualTransaction(false, amount, description, programId)
                showAddTransactionSheet = false
            },
        )
    }

    if (showTagsSheet) {
        CustomerTagsSheet(
            currentTags = state.relation?.tags.orEmpty(),
            suggestedTags = state.suggestedTags,
            isSaving = state.isSaving,
            onDismiss = { showTagsSheet = false },
            onSave = { tags ->
                viewModel.updateTags(tags)
                showTagsSheet = false
            },
        )
    }

    if (showMergeSheet && customer != null) {
        CustomerMergeDuplicateSheet(
            currentCustomer = customer,
            candidates = state.availableDuplicateCustomers,
            preview = state.mergePreview,
            isLoading = state.isDuplicateResolutionLoading,
            onDismiss = {
                showMergeSheet = false
                viewModel.clearMergePreview()
            },
            onSelectCandidate = viewModel::previewMerge,
            onConfirm = {
                viewModel.mergeWithPreviewTarget()
                showMergeSheet = false
            },
        )
    }

    if (showSplitSheet && customer != null) {
        CustomerSplitIdentitySheet(
            customer = customer,
            preview = state.splitPreview,
            isLoading = state.isDuplicateResolutionLoading,
            onDismiss = {
                showSplitSheet = false
                viewModel.clearSplitPreview()
            },
            onSubmit = { firstName, lastName, phoneNumber, email, notes ->
                viewModel.splitIdentity(firstName, lastName, phoneNumber, email, notes)
                showSplitSheet = false
            },
        )
    }

    LaunchedEffect(state.feedbackMessageRes) {
        if (state.feedbackMessageRes != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.consumeFeedback()
        }
    }

    LaunchedEffect(showSplitSheet) {
        if (showSplitSheet) {
            viewModel.previewSplit()
        }
    }

    LaunchedEffect(state.duplicateResolutionNavigationCustomerId) {
        state.duplicateResolutionNavigationCustomerId?.let { customerId ->
            viewModel.consumeDuplicateResolutionNavigation()
            onOpenCustomer(customerId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CustomerFeatureScaffold(
            title = customer?.displayName().orEmpty().ifBlank { stringResource(R.string.merchant_customer_profile) },
            subtitle = customer?.email.orEmpty().ifBlank { stringResource(R.string.merchant_customer_profile_missing_subtitle) },
            onBack = onBack,
            headerStyle = CustomerFeatureHeaderStyle.GRADIENT,
            showTitle = false,
            backLabel = stringResource(R.string.auth_back),
            wrapBodyInSheet = false,
            headerContent = {
                customer?.let {
                    CustomerProfileHero(
                        customer = it,
                        relation = state.relation,
                        scopedLastVisit = state.scopedLastVisit,
                        showTierBadge = hasActiveTierProgram,
                        onEditTags = { showTagsSheet = true },
                    )
                }
                if (customer != null) {
                    CustomerProfileTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                }
            },
            body = {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 20.dp,
                            bottom = contentPadding.calculateBottomPadding() + 96.dp,
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
                                when (selectedTab) {
                                    CustomerProfileTab.OVERVIEW -> {
                                        item {
                                            CustomerProfileBalanceSection(
                                                customer = customer,
                                                scopedVisits = state.scopedVisits,
                                                scopedSpent = state.scopedSpent,
                                                progress = state.tierProgress,
                                                nextTierThreshold = state.nextTierThreshold,
                                                showTierProgress = hasActiveTierProgram,
                                                onOpenBonusManager = {
                                                    if (availablePointsPrograms.isEmpty()) {
                                                        viewModel.showProgramRequiredFeedback()
                                                    } else {
                                                        showAdjustPoints = true
                                                    }
                                                },
                                                onOpenTransactions = {
                                                    if (availablePointsPrograms.isEmpty()) {
                                                        viewModel.showProgramRequiredFeedback()
                                                    } else {
                                                        showAddTransactionSheet = true
                                                    }
                                                },
                                            )
                                        }
                                        item { CustomerProfileStatsGrid(state.scopedVisits, state.scopedSpent) }
                                        item {
                                            CustomerProfileContactSection(
                                                customer = customer,
                                                activeStoreName = state.activeStoreName,
                                                activeStoreAddress = state.activeStoreAddress,
                                                onEditContact = { showEditContact = true },
                                            )
                                        }
                                        if (state.canManageDuplicates) {
                                            item {
                                                CustomerDuplicateResolutionSection(
                                                    mergePreview = state.mergePreview,
                                                    splitPreview = state.splitPreview,
                                                    onOpenMerge = {
                                                        viewModel.clearMergePreview()
                                                        showMergeSheet = true
                                                    },
                                                    onOpenSplit = {
                                                        viewModel.clearSplitPreview()
                                                        showSplitSheet = true
                                                    },
                                                )
                                            }
                                        }
                                        item { CustomerProfileNotesPreviewSection(state.relation, onEditNotes = { showEditCrm = true }) }
                                        if (state.activities.isNotEmpty()) {
                                            item {
                                                CustomerProfileActivityPreviewSection(
                                                    activities = state.activities,
                                                    onOpenActivity = { selectedTab = CustomerProfileTab.ACTIVITY },
                                                )
                                            }
                                        }
                                    }
                                    CustomerProfileTab.TRANSACTIONS -> {
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
                                    CustomerProfileTab.ACTIVITY -> {
                                        if (state.activities.isNotEmpty()) {
                                            item { CustomerActivitySection(state.activities, onOpenTransaction) }
                                        } else {
                                            item {
                                                MerchantEmptyStateCard(
                                                    title = stringResource(R.string.merchant_customer_activity_empty_title),
                                                    subtitle = stringResource(R.string.merchant_customer_activity_empty_subtitle),
                                                    icon = Icons.Default.Person,
                                                )
                                            }
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
                                        } else {
                                            item {
                                                MerchantEmptyStateCard(
                                                    title = stringResource(R.string.merchant_customer_access_empty_title),
                                                    subtitle = stringResource(R.string.merchant_customer_access_empty_subtitle),
                                                    icon = Icons.Default.CreditCard,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (customer != null && selectedTab != CustomerProfileTab.ACCESS) {
                        FloatingActionButton(
                            onClick = {
                                if (availablePointsPrograms.isEmpty()) {
                                    viewModel.showProgramRequiredFeedback()
                                } else {
                                    showAddTransactionSheet = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 24.dp, bottom = contentPadding.calculateBottomPadding() + 18.dp),
                            containerColor = VerevColors.Gold,
                            contentColor = VerevColors.Forest,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.merchant_customer_add_transaction),
                            )
                        }
                    }
                }
            },
        )
        MerchantLoadingOverlay(
            isVisible = state.isSaving,
            title = stringResource(R.string.merchant_loader_customer_profile_title),
            subtitle = stringResource(R.string.merchant_loader_customer_profile_subtitle),
        )
    }

    state.duplicateResolutionSuccessMessageRes?.let { messageRes ->
        MerchantSuccessDialog(
            message = stringResource(messageRes),
            onDismiss = viewModel::clearDuplicateResolutionFeedback,
        )
    }

    state.duplicateResolutionErrorMessage?.let { message ->
        MerchantErrorDialog(
            message = message,
            onDismiss = viewModel::clearDuplicateResolutionFeedback,
        )
    }
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
        headerStyle = CustomerFeatureHeaderStyle.GRADIENT,
        showTitle = false,
        headerContent = {
            transaction?.let { CustomerTransactionDetailHero(it) }
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
                    else -> item {
                        CustomerTransactionDetailSection(
                            transaction = transaction,
                            voidRequest = state.voidRequest,
                            canRequestVoid = state.canRequestVoid,
                            onRequestVoid = viewModel::showVoidDialog,
                        )
                    }
                }
            }
        },
    )

    if (state.showVoidDialog && transaction != null) {
        RequestTransactionVoidDialog(
            reason = state.voidReason,
            reasonError = state.voidReasonError?.let { stringResource(it) },
            isSubmitting = state.isSubmittingVoid,
            onDismiss = viewModel::dismissVoidDialog,
            onReasonChange = viewModel::updateVoidReason,
            onSubmit = viewModel::submitVoidRequest,
        )
    }

    state.successMessageRes?.let { messageRes ->
        MerchantSuccessDialog(
            message = stringResource(messageRes),
            onDismiss = viewModel::clearFeedback,
        )
    }

    state.errorMessageRes?.let { messageRes ->
        MerchantErrorDialog(
            message = stringResource(messageRes),
            onDismiss = viewModel::clearFeedback,
        )
    }
}
