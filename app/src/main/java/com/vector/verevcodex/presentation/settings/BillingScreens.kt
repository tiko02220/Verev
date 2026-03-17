package com.vector.verevcodex.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vector.verevcodex.R
import com.vector.verevcodex.presentation.merchant.common.MerchantErrorDialog
import com.vector.verevcodex.presentation.merchant.common.MerchantLoadingOverlay
import com.vector.verevcodex.presentation.merchant.common.MerchantSuccessDialog
import com.vector.verevcodex.presentation.theme.VerevColors

@Composable
fun PlanSelectionScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: PlanSelectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentPlanSpec = BillingPlanUiCatalog.specFor(state.currentPlanKey)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { SettingsBackRow(onBack = onBack) }
            item {
                SettingsHeroCard(
                    title = stringResource(R.string.merchant_plan_selection_title),
                    subtitle = stringResource(R.string.merchant_plan_selection_subtitle, stringResource(currentPlanSpec.nameRes)),
                    icon = Icons.Default.Subscriptions,
                    colors = listOf(VerevColors.Gold, VerevColors.Tan),
                )
            }
            items(state.options, key = { it.id }) { plan ->
                SettingsDetailSection(title = stringResource(plan.nameRes)) {
                    SettingsDetailRow(
                        label = stringResource(R.string.merchant_payment_methods_plan_title),
                        value = plan.priceLabel,
                        trailing = {
                            if (plan.isSelected) {
                                SettingsSectionBadge(text = stringResource(R.string.merchant_current_location))
                            }
                        },
                    )
                    Text(
                        text = stringResource(plan.summaryRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerevColors.Forest.copy(alpha = 0.72f),
                    )
                    plan.featureResIds.forEach { featureRes ->
                        Text(
                            text = "\u2022 ${stringResource(featureRes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = VerevColors.Forest.copy(alpha = 0.68f),
                        )
                    }
                    Button(
                        onClick = { viewModel.selectPlan(plan.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !plan.isSelected && !state.isSaving,
                    ) {
                        Text(
                            text = if (plan.isSelected) {
                                stringResource(R.string.merchant_plan_selection_current)
                            } else {
                                stringResource(R.string.merchant_plan_selection_select)
                            }
                        )
                    }
                }
            }
        }
        MerchantLoadingOverlay(
            isVisible = state.isSaving,
            title = stringResource(R.string.merchant_loader_plan_title),
            subtitle = stringResource(R.string.merchant_loader_plan_subtitle),
        )
    }
    state.messageRes?.let { messageRes ->
        MerchantSuccessDialog(
            message = stringResource(messageRes),
            onDismiss = viewModel::dismissMessage,
        )
    }
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = viewModel::dismissMessage,
        )
    }
}

@Composable
fun AllInvoicesScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    onOpenInvoice: (String) -> Unit,
    viewModel: InvoicesViewModel = hiltViewModel(),
) {
    val state by viewModel.allInvoicesUiState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_all_invoices_title),
                subtitle = stringResource(R.string.merchant_all_invoices_subtitle),
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                colors = listOf(VerevColors.Forest, VerevColors.Moss),
            )
        }
        items(state.invoices, key = { it.id }) { invoice ->
            SettingsDetailSection(title = invoice.title) {
                SettingsMenuRow(
                    title = invoice.subtitle,
                    subtitle = invoice.amount,
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    trailingLabel = stringResource(invoice.statusRes),
                    onClick = { onOpenInvoice(invoice.id) },
                )
            }
        }
    }
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = onBack,
        )
    }
}

@Composable
fun InvoiceDetailScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onBack: () -> Unit,
    viewModel: InvoicesViewModel = hiltViewModel(),
) {
    val state by viewModel.invoiceDetailUiState.collectAsStateWithLifecycle()
    val statusText = state.statusRes?.let { stringResource(it) }.orEmpty()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SettingsBackRow(onBack = onBack) }
        item {
            SettingsHeroCard(
                title = stringResource(R.string.merchant_invoice_detail_title),
                subtitle = state.invoice?.title ?: stringResource(R.string.merchant_invoice_detail_subtitle),
                icon = Icons.Default.Payments,
                colors = listOf(VerevColors.Tan, VerevColors.Gold),
            )
        }
        state.invoice?.let { invoice ->
            item {
                SettingsDetailSection(title = invoice.title) {
                    SettingsDetailRow(
                        label = stringResource(R.string.merchant_invoice_detail_period),
                        value = invoice.subtitle,
                    )
                    SettingsDetailRow(
                        label = stringResource(R.string.merchant_invoice_detail_amount),
                        value = state.amountLabel,
                    )
                    SettingsDetailRow(
                        label = stringResource(R.string.merchant_invoice_detail_status),
                        value = statusText,
                    )
                    SettingsDetailRow(
                        label = stringResource(R.string.merchant_invoice_detail_issued_date),
                        value = state.issuedDateLabel,
                    )
                }
            }
        }
    }
    state.errorRes?.let { errorRes ->
        MerchantErrorDialog(
            message = stringResource(errorRes),
            onDismiss = onBack,
        )
    }
}
