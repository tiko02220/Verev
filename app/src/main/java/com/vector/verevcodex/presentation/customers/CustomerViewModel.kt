package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.usecase.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.ObserveCustomersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerViewModel @Inject constructor(
    observeCustomersUseCase: ObserveCustomersUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Customer>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Customer>>> = _uiState.asStateFlow()

    init {
        observeCustomersUseCase()
            .onEach { _uiState.value = if (it.isEmpty()) UiState.Empty else UiState.Success(it) }
            .catch { _uiState.value = UiState.Error(it.message ?: "Failed to load customers") }
            .launchIn(viewModelScope)
    }

    fun rewardLoyaltyBoost(customerId: String) {
        viewModelScope.launch { adjustCustomerPointsUseCase(customerId, 20, "Manual loyalty adjustment") }
    }
}
