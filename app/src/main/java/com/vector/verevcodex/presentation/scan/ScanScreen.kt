package com.vector.verevcodex.presentation.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.vector.verevcodex.presentation.merchant.common.MerchantEmptyStateCard
import com.vector.verevcodex.presentation.merchant.common.MerchantFormField
import com.vector.verevcodex.presentation.merchant.common.MerchantPageHeader
import com.vector.verevcodex.presentation.merchant.common.MerchantPrimaryCard

@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    contentPadding: PaddingValues = PaddingValues(),
    onOpenCustomer: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var firstName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var points by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.customer?.id) {
        if (state.customer != null) {
            firstName = ""
            phone = ""
        }
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
        item {
            MerchantPageHeader(
                title = stringResource(R.string.merchant_scan_title),
                subtitle = stringResource(R.string.merchant_scan_subtitle),
            )
        }
        item {
            ScanIntroCard(storeName = state.selectedStoreName.ifBlank { stringResource(R.string.merchant_select_store) })
        }
        item {
            MerchantPrimaryCard {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.merchant_scan_prompt_title),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    color = com.vector.verevcodex.presentation.theme.VerevColors.Forest,
                )
                androidx.compose.material3.Text(
                    text = stringResource(R.string.merchant_scan_prompt_subtitle),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = com.vector.verevcodex.presentation.theme.VerevColors.Forest.copy(alpha = 0.64f),
                )
                ScanTriggerButtons(
                    onKnown = {
                        focusManager.clearFocus(force = true)
                        viewModel.simulateKnownScan()
                    },
                    onNew = {
                        focusManager.clearFocus(force = true)
                        viewModel.simulateNewScan()
                    },
                )
                state.scannedId?.let { scannedId ->
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.merchant_scan_last_id, scannedId),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = com.vector.verevcodex.presentation.theme.VerevColors.Forest.copy(alpha = 0.58f),
                    )
                }
            }
        }

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
                    MerchantPrimaryCard {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.merchant_scan_action_title),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = com.vector.verevcodex.presentation.theme.VerevColors.Forest,
                        )
                        ScanActionChips(
                            selectedAction = state.selectedAction,
                            onActionSelected = viewModel::selectAction,
                        )
                        when (state.selectedAction) {
                            ScanCustomerAction.ADD_POINTS -> {
                                MerchantFormField(
                                    value = amount,
                                    onValueChange = { amount = it },
                                    label = stringResource(R.string.merchant_scan_amount_label),
                                    leadingIcon = Icons.Default.CreditCard,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done,
                                    ),
                                )
                                androidx.compose.material3.Text(
                                    text = stringResource(R.string.merchant_scan_points_preview, calculatePreviewPoints(amount)),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = com.vector.verevcodex.presentation.theme.VerevColors.Forest.copy(alpha = 0.6f),
                                )
                            }
                            ScanCustomerAction.REDEEM_POINTS -> {
                                MerchantFormField(
                                    value = points,
                                    onValueChange = { points = it },
                                    label = stringResource(R.string.merchant_scan_points_label),
                                    leadingIcon = Icons.Default.CreditCard,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done,
                                    ),
                                )
                                androidx.compose.material3.Text(
                                    text = stringResource(R.string.merchant_scan_redeem_supporting, state.customer!!.currentPoints),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = com.vector.verevcodex.presentation.theme.VerevColors.Forest.copy(alpha = 0.6f),
                                )
                            }
                            ScanCustomerAction.CHECK_IN -> {
                                androidx.compose.material3.Text(
                                    text = stringResource(R.string.merchant_scan_check_in_supporting),
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                    color = com.vector.verevcodex.presentation.theme.VerevColors.Forest.copy(alpha = 0.64f),
                                )
                            }
                        }
                        ScanPrimaryActions(
                            isSubmitting = state.isSubmitting,
                            onApply = {
                                focusManager.clearFocus(force = true)
                                viewModel.submitAction(amountInput = amount, pointsInput = points)
                            },
                            onOpenProfile = { onOpenCustomer(state.customer!!.id) },
                        )
                    }
                }
            }
            state.scannedId != null -> {
                item {
                    MerchantPrimaryCard {
                        androidx.compose.material3.Text(
                            text = stringResource(R.string.merchant_scan_quick_register_title),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = com.vector.verevcodex.presentation.theme.VerevColors.Forest,
                        )
                        MerchantFormField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = stringResource(R.string.merchant_scan_quick_register_name),
                            leadingIcon = Icons.Default.CreditCard,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                            ),
                        )
                        MerchantFormField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = stringResource(R.string.merchant_scan_quick_register_phone),
                            leadingIcon = Icons.Default.CreditCard,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done,
                            ),
                        )
                        androidx.compose.material3.Button(
                            onClick = {
                                focusManager.clearFocus(force = true)
                                viewModel.quickRegister(firstName, phone)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSubmitting,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = com.vector.verevcodex.presentation.theme.VerevColors.Gold,
                                contentColor = androidx.compose.ui.graphics.Color.White,
                            ),
                        ) {
                            androidx.compose.material3.Text(stringResource(R.string.merchant_scan_quick_register_submit))
                        }
                    }
                }
            }
        }
    }
}

private fun calculatePreviewPoints(amountInput: String): Int {
    val amount = amountInput.toDoubleOrNull() ?: return 0
    return (amount / 100.0).toInt().coerceAtLeast(1)
}
