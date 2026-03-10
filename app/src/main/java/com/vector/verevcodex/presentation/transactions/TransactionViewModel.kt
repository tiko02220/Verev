package com.vector.verevcodex.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.TransactionItem
import com.vector.verevcodex.domain.usecase.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.ObserveStoresUseCase
import com.vector.verevcodex.domain.usecase.ObserveTransactionsUseCase
import com.vector.verevcodex.domain.usecase.RecordTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class TransactionViewModel @Inject constructor(
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
    private val observeCustomersUseCase: ObserveCustomersUseCase,
    private val observeStoresUseCase: ObserveStoresUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Transaction>>> = _uiState.asStateFlow()

    init {
        observeTransactionsUseCase()
            .onEach { _uiState.value = UiState.Success(it) }
            .catch { _uiState.value = UiState.Error(it.message ?: "Failed to load transactions") }
            .launchIn(viewModelScope)
    }

    fun addDemoTransaction() {
        viewModelScope.launch {
            val customer = observeCustomersUseCase().first().first()
            val store = observeStoresUseCase().first().first()
            val transactionId = UUID.randomUUID().toString()
            recordTransactionUseCase(
                Transaction(
                    id = transactionId,
                    customerId = customer.id,
                    storeId = store.id,
                    staffId = "33333333-3333-3333-3333-333333333333",
                    amount = 9200.0,
                    pointsEarned = 92,
                    pointsRedeemed = 0,
                    timestamp = LocalDateTime.now(),
                    metadata = "Quick checkout sale",
                    items = listOf(TransactionItem(UUID.randomUUID().toString(), transactionId, "Checkout item", 1, 9200.0)),
                )
            )
        }
    }
}
