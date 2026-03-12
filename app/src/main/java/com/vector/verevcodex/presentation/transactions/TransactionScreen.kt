package com.vector.verevcodex.presentation.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.theme.VerevColors
import kotlinx.coroutines.delay

@Composable
fun TransactionEntryScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit = {},
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.successRes, state.errorRes) {
        if (state.successRes != null || state.errorRes != null) {
            delay(2500)
            viewModel.clearFeedback()
        }
    }

    val fieldErrors = state.fieldErrors.mapValues { (_, value) -> stringResource(value) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerevColors.AppBackground),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(VerevColors.ForestDeep, VerevColors.Forest, VerevColors.Moss)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            TransactionHeaderSection(
                title = stringResource(R.string.merchant_transaction_title),
                subtitle = stringResource(R.string.merchant_transaction_subtitle, state.selectedStoreName.ifBlank { stringResource(R.string.merchant_transaction_context_pending) }),
                storeName = state.selectedStoreName,
                cashierName = state.cashierName,
                onBack = onBack,
            )
        }

        androidx.compose.material3.Surface(
            modifier = Modifier.weight(1f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = VerevColors.AppBackground,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding(),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 18.dp,
                    bottom = contentPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {

        state.errorRes?.let { errorRes ->
            item {
                CheckoutFeedbackCard(
                    text = stringResource(errorRes),
                    positive = false,
                )
            }
        }

        state.successRes?.let { successRes ->
            item {
                CheckoutFeedbackCard(
                    text = stringResource(successRes),
                    positive = true,
                )
            }
        }

        item {
            CustomerSelectionCard(
                query = state.customerQuery,
                selectedCustomer = state.selectedCustomer,
                customers = state.filteredCustomers,
                customerError = fieldErrors[TRANSACTION_FIELD_CUSTOMER],
                onQueryChanged = viewModel::updateCustomerQuery,
                onCustomerSelected = viewModel::selectCustomer,
                onClearSelectedCustomer = viewModel::clearSelectedCustomer,
            )
        }

        item {
            CheckoutLineItemsCard(
                lineItems = state.lineItems,
                fieldErrors = fieldErrors,
                onAddLineItem = viewModel::addLineItem,
                onRemoveLineItem = viewModel::removeLineItem,
                onNameChanged = viewModel::updateLineItemName,
                onQuantityChanged = viewModel::updateLineItemQuantity,
                onPriceChanged = viewModel::updateLineItemUnitPrice,
            )
        }

        item {
            CheckoutPromotionCard(
                promotions = state.availablePromotions,
                selectedPromotionId = state.selectedPromotionId,
                onPromotionSelected = viewModel::selectPromotion,
            )
        }

        item {
            CheckoutLoyaltyCard(
                selectedCustomer = state.selectedCustomer,
                applyRedemption = state.applyPointRedemption,
                redeemPointsInput = state.redeemPointsInput,
                totals = state.totals,
                redeemError = fieldErrors[TRANSACTION_FIELD_REDEEM],
                onToggleRedemption = viewModel::togglePointRedemption,
                onRedeemPointsChanged = viewModel::updateRedeemPoints,
            )
        }

        item {
            CheckoutSummaryCard(
                note = state.note,
                onNoteChanged = viewModel::updateNote,
                totals = state.totals,
                isSubmitting = state.isSubmitting,
                onSubmit = viewModel::submitCheckout,
            )
        }

                item {
                    RecentTransactionsCard(records = state.recentTransactions)
                }
            }
        }
    }
}
