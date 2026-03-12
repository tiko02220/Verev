package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.transactions.ObserveTransactionUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class CustomerTransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTransactionUseCase: ObserveTransactionUseCase,
) : ViewModel() {
    private val transactionId: String? = savedStateHandle[Screen.CustomerTransactionDetail.ARG_TRANSACTION_ID]

    private val _uiState = MutableStateFlow(CustomerTransactionDetailUiState())
    val uiState: StateFlow<CustomerTransactionDetailUiState> = _uiState.asStateFlow()

    init {
        val currentTransactionId = transactionId
        if (currentTransactionId.isNullOrBlank()) {
            _uiState.value = CustomerTransactionDetailUiState(isMissingTransaction = true)
        } else {
            observeTransactionUseCase(currentTransactionId)
                .onEach { transaction ->
                    _uiState.value = CustomerTransactionDetailUiState(
                        transaction = transaction,
                        isMissingTransaction = transaction == null,
                    )
                }
                .launchIn(viewModelScope)
        }
    }
}
