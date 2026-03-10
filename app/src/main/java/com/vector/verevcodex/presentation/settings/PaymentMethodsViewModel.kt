package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.ViewModel
import com.vector.verevcodex.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(
        PaymentMethodsUiState(
            methods = listOf(
                PaymentMethodUi(id = "pm_visa_4242", brand = "Visa", last4 = "4242", expiry = "12/27", isDefault = true),
                PaymentMethodUi(id = "pm_mc_1234", brand = "Mastercard", last4 = "1234", expiry = "09/28", isDefault = false),
            ),
            invoices = listOf(
                BillingEntryUi(id = "inv_2026_02", title = "Premium plan", subtitle = "February 2026", amount = "$99.00", status = "Paid"),
                BillingEntryUi(id = "inv_2026_01", title = "Premium plan", subtitle = "January 2026", amount = "$99.00", status = "Paid"),
                BillingEntryUi(id = "inv_2025_12", title = "Premium plan", subtitle = "December 2025", amount = "$99.00", status = "Paid"),
            ),
        ),
    )
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    fun addCard() {
        _uiState.update { state ->
            val index = state.methods.size + 1
            state.copy(
                methods = state.methods + PaymentMethodUi(
                    id = "pm_auto_$index",
                    brand = if (index % 2 == 0) "Visa" else "Mastercard",
                    last4 = (4000 + index).toString(),
                    expiry = "11/29",
                    isDefault = state.methods.isEmpty(),
                ),
                messageRes = R.string.merchant_payment_methods_message_card_added,
            )
        }
    }

    fun makeDefault(methodId: String) {
        _uiState.update { state ->
            state.copy(
                methods = state.methods.map { method -> method.copy(isDefault = method.id == methodId) },
                messageRes = R.string.merchant_payment_methods_message_default_updated,
            )
        }
    }

    fun removeMethod(methodId: String) {
        _uiState.update { state ->
            val remaining = state.methods.filterNot { it.id == methodId }
            val adjusted = if (remaining.none { it.isDefault } && remaining.isNotEmpty()) {
                remaining.mapIndexed { index, method -> method.copy(isDefault = index == 0) }
            } else {
                remaining
            }
            state.copy(methods = adjusted, messageRes = R.string.merchant_payment_methods_message_removed)
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(messageRes = null) }
    }
}

data class PaymentMethodsUiState(
    val planPrice: String = "$99/mo",
    val renewalLabel: String = "Apr 1, 2026",
    val methods: List<PaymentMethodUi> = emptyList(),
    val invoices: List<BillingEntryUi> = emptyList(),
    val messageRes: Int? = null,
)
