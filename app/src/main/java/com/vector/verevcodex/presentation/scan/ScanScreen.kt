package com.vector.verevcodex.presentation.scan

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.scan.ScanMethod
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.theme.VerevColors

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

    val amount = rememberSaveable { mutableStateOf("") }
    val points = rememberSaveable { mutableStateOf("") }

    LaunchedEffect(initialMethod) {
        viewModel.prepareScanEntry(initialMethod)
    }

    if (state.contentMode == ScanContentMode.ACTIVE_SCAN && state.activeScanMethod != null) {
        ActiveScanSurface(
            contentPadding = contentPadding,
            activeMethod = state.activeScanMethod!!,
            scanSessionToken = state.scanSessionToken,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
    ) {
        ScanRouteHeader(
            title = stringResource(R.string.merchant_scan_title),
            onBack = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 0.dp,
                end = 0.dp,
                top = 0.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.messageRes?.let { messageRes ->
                item {
                    ScanInlineBanner(
                        title = stringResource(messageRes),
                        subtitle = stringResource(R.string.merchant_scan_success_supporting),
                        positive = true,
                    )
                }
            }
            state.errorRes?.let { errorRes ->
                item {
                    ScanInlineBanner(
                        title = stringResource(errorRes),
                        subtitle = stringResource(R.string.merchant_scan_error_supporting),
                        positive = false,
                    )
                }
            }

            when {
                state.contentMode == ScanContentMode.LOOKUP -> {
                    item {
                        ScanCenteredStatusSurface(
                            title = stringResource(R.string.merchant_scan_searching_title),
                            subtitle = stringResource(R.string.merchant_scan_searching_subtitle),
                            icon = Icons.Default.CreditCard,
                        )
                    }
                }

                state.contentMode == ScanContentMode.CUSTOMER && state.customer != null -> {
                    item { ScanCustomerCard(customer = state.customer!!) }
                    item {
                        ScanActionComposerCard(
                            activePrograms = state.activePrograms,
                            availableActions = state.availableActions,
                            selectedAction = state.selectedAction,
                            amount = amount.value,
                            points = points.value,
                            customerPoints = state.customer!!.currentPoints,
                            fieldErrors = state.fieldErrors,
                            isSubmitting = state.isSubmitting,
                            onAmountChanged = {
                                amount.value = it
                                viewModel.clearFieldErrors(SCAN_FIELD_AMOUNT)
                            },
                            onPointsChanged = {
                                points.value = it
                                viewModel.clearFieldErrors(SCAN_FIELD_POINTS)
                            },
                            onActionSelected = viewModel::selectAction,
                            onApply = {
                                focusManager.clearFocus(force = true)
                                viewModel.submitAction(amountInput = amount.value, pointsInput = points.value)
                            },
                            onOpenProfile = { onOpenCustomer(state.customer!!.id) },
                            onScanAnother = {
                                amount.value = ""
                                points.value = ""
                                focusManager.clearFocus(force = true)
                                viewModel.requestScan()
                            },
                        )
                    }
                }
                else -> {
                    item {
                        ScanCenteredStatusSurface(
                            title = stringResource(R.string.merchant_scan_searching_title),
                            subtitle = stringResource(R.string.merchant_scan_searching_subtitle),
                            icon = Icons.Default.CreditCard,
                        )
                    }
                }
            }
        }
    }
}
