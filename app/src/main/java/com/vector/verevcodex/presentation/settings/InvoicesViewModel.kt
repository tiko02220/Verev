package com.vector.verevcodex.presentation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.usecase.settings.ObserveInvoicesUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InvoicesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeInvoicesUseCase: ObserveInvoicesUseCase,
) : ViewModel() {
    private val invoiceId: String? = savedStateHandle[Screen.InvoiceDetail.ARG_INVOICE_ID]

    val allInvoicesUiState: StateFlow<AllInvoicesUiState> = observeSelectedStoreUseCase()
        .flatMapLatest { store ->
            val ownerId = store?.ownerId
            if (ownerId == null) {
                flowOf(AllInvoicesUiState(errorRes = R.string.merchant_payment_methods_error_store_missing))
            } else {
                observeInvoicesUseCase(ownerId).map { invoices ->
                    AllInvoicesUiState(invoices = invoices.map { it.toUi() })
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AllInvoicesUiState())

    val invoiceDetailUiState: StateFlow<InvoiceDetailUiState> = observeSelectedStoreUseCase()
        .flatMapLatest { store ->
            val ownerId = store?.ownerId
            if (ownerId == null || invoiceId.isNullOrBlank()) {
                flowOf(InvoiceDetailUiState(errorRes = R.string.merchant_invoice_detail_error))
            } else {
                observeInvoicesUseCase(ownerId).map { invoices ->
                    val invoice = invoices.firstOrNull { it.id == invoiceId }
                    if (invoice == null) {
                        InvoiceDetailUiState(errorRes = R.string.merchant_invoice_detail_error)
                    } else {
                        InvoiceDetailUiState(
                            invoice = invoice.toUi(),
                            issuedDateLabel = invoice.issuedDateLabel(),
                            amountLabel = invoice.amountLabel(),
                            statusRes = invoice.toUi().statusRes,
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceDetailUiState())
}
