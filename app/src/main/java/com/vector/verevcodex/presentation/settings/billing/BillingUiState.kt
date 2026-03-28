package com.vector.verevcodex.presentation.settings.billing

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes

data class SubscriptionPlanOptionUi(
    val id: String,
    @StringRes val nameRes: Int,
    val priceLabel: String,
    @StringRes val summaryRes: Int,
    val featureResIds: List<Int>,
    val isSelected: Boolean,
)

data class PlanSelectionUiState(
    val currentPlanKey: String = "",
    val currentPlanPrice: String = "",
    val options: List<SubscriptionPlanOptionUi> = emptyList(),
    val isSaving: Boolean = false,
    val messageRes: Int? = null,
    val errorRes: Int? = null,
)

data class AllInvoicesUiState(
    val invoices: List<BillingEntryUi> = emptyList(),
    val errorRes: Int? = null,
)

data class InvoiceDetailUiState(
    val invoice: BillingEntryUi? = null,
    val issuedDateLabel: String = "",
    val amountLabel: String = "",
    val statusRes: Int? = null,
    val errorRes: Int? = null,
)
