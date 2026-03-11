package com.vector.verevcodex.presentation.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.RewardProgramScanAction
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    initialMethod: ScanMethod? = null,
    onBack: () -> Unit = {},
    onOpenCustomer: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var firstName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var points by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialMethod) {
        viewModel.prepareScanEntry(initialMethod)
    }

    LaunchedEffect(state.customer?.id) {
        if (state.customer != null) {
            firstName = ""
            phone = ""
        }
    }

    val showDedicatedScanSurface =
        state.activeScanMethod != null &&
            state.customer == null &&
            !state.isSearching &&
            (state.scannedLoyaltyId == null || state.errorRes != null)

    if (showDedicatedScanSurface) {
        ActiveScanSurface(
            contentPadding = contentPadding,
            activeMethod = state.activeScanMethod!!,
            errorRes = state.errorRes,
            messageRes = state.messageRes,
            onSelectMethod = viewModel::switchScanMethod,
            onBarcodeScanned = viewModel::onBarcodeScanned,
            onBarcodeFailed = viewModel::onBarcodeScanFailed,
            onRetry = viewModel::requestScan,
            onCancel = onBack,
        )
        return
    }

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
        state.messageRes?.let { messageRes ->
            item {
                ScanFeedbackCard(
                    title = stringResource(messageRes),
                    subtitle = stringResource(R.string.merchant_scan_success_supporting),
                    positive = true,
                )
            }
        }
        state.errorRes?.let { errorRes ->
            item {
                ScanFeedbackCard(
                    title = stringResource(errorRes),
                    subtitle = stringResource(R.string.merchant_scan_error_supporting),
                    positive = false,
                )
            }
        }

        when {
            state.isSearching -> {
                item {
                    MerchantEmptyStateCard(
                        title = stringResource(R.string.merchant_scan_searching_title),
                        subtitle = stringResource(R.string.merchant_scan_searching_subtitle),
                        icon = Icons.Default.CreditCard,
                    )
                }
            }

            state.customer != null -> {
                item { ScanCustomerCard(customer = state.customer!!) }
                item {
                    ScanActionComposerCard(
                        availableActions = state.availableActions,
                        selectedAction = state.selectedAction,
                        amount = amount,
                        points = points,
                        customerPoints = state.customer!!.currentPoints,
                        isSubmitting = state.isSubmitting,
                        onAmountChanged = { amount = it },
                        onPointsChanged = { points = it },
                        onActionSelected = viewModel::selectAction,
                        onApply = {
                            focusManager.clearFocus(force = true)
                            viewModel.submitAction(amountInput = amount, pointsInput = points)
                        },
                        onOpenProfile = { onOpenCustomer(state.customer!!.id) },
                        onScanAnother = {
                            amount = ""
                            points = ""
                            focusManager.clearFocus(force = true)
                            viewModel.requestScan()
                        },
                    )
                }
            }

            state.scannedLoyaltyId != null -> {
                item {
                    ScanQuickRegisterCard(
                        loyaltyId = state.scannedLoyaltyId!!,
                        firstName = firstName,
                        phone = phone,
                        isSubmitting = state.isSubmitting,
                        onFirstNameChanged = { firstName = it },
                        onPhoneChanged = { phone = it },
                        onSubmit = {
                            focusManager.clearFocus(force = true)
                            viewModel.quickRegister(firstName, phone)
                        },
                    )
                }
            }
        }
    }
}

private fun calculatePreviewPoints(amountInput: String): Int {
    val amount = amountInput.toDoubleOrNull() ?: return 0
    return (amount / 100.0).toInt().coerceAtLeast(1)
}

private fun calculateCashbackPreview(amountInput: String): Int {
    val amount = amountInput.toDoubleOrNull() ?: return 0
    return (amount * 0.05).toInt().coerceAtLeast(1)
}
