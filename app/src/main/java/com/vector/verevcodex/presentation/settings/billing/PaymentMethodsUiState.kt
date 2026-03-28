package com.vector.verevcodex.presentation.settings.billing

import com.vector.verevcodex.presentation.settings.*

import androidx.annotation.StringRes

data class PaymentMethodsUiState(
    val planKey: String = "",
    val planPrice: String = "",
    val renewalLabel: String = "",
    val methods: List<PaymentMethodUi> = emptyList(),
    val invoices: List<BillingEntryUi> = emptyList(),
    val isSaving: Boolean = false,
    @StringRes val messageRes: Int? = null,
    @StringRes val errorRes: Int? = null,
) {
    val isEmptyState: Boolean get() = methods.isEmpty() && invoices.isEmpty()
}
