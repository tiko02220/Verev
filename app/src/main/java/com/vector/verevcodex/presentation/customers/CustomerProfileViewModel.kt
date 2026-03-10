package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.usecase.ObserveCustomerUseCase
import com.vector.verevcodex.domain.usecase.ObserveTransactionsUseCase
import com.vector.verevcodex.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCustomerUseCase: ObserveCustomerUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
) : ViewModel() {
    private val customerId: String? = savedStateHandle[Screen.CustomerProfile.ARG_CUSTOMER_ID]

    private val _uiState = MutableStateFlow(CustomerProfileUiState())
    val uiState: StateFlow<CustomerProfileUiState> = _uiState.asStateFlow()

    init {
        val currentCustomerId = customerId
        if (currentCustomerId.isNullOrBlank()) {
            _uiState.value = CustomerProfileUiState(isMissingCustomer = true)
        } else {
            combine(
                observeCustomerUseCase(currentCustomerId),
                observeTransactionsUseCase(),
            ) { customer, transactions ->
                CustomerProfileUiState(
                    customer = customer,
                    transactions = transactions
                        .filter { it.customerId == currentCustomerId }
                        .sortedByDescending { it.timestamp },
                )
            }.onEach { _uiState.value = it }.launchIn(viewModelScope)
        }
    }
}

data class CustomerProfileUiState(
    val customer: Customer? = null,
    val transactions: List<Transaction> = emptyList(),
    val isMissingCustomer: Boolean = false,
)
