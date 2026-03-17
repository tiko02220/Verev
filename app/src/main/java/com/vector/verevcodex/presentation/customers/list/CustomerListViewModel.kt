package com.vector.verevcodex.presentation.customers.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsByStoreUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.presentation.customers.CustomerListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerListViewModel @Inject constructor(
    private val observeCustomersUseCase: ObserveCustomersUseCase,
    private val observeCustomerRelationsByStoreUseCase: ObserveCustomerRelationsByStoreUseCase,
    private val observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val observeProgramsUseCase: ObserveProgramsUseCase,
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedTier = MutableStateFlow<LoyaltyTier?>(null)

    init {
        observeData()
    }

    private fun observeData() {
        val selectedStoreFlow = observeSelectedStoreUseCase()

        // Combine logic moved here from the flat directory to keep it cohesive
        combine(
            selectedStoreFlow,
            searchQuery,
            selectedTier,
            selectedStoreFlow.flatMapLatest { store -> observeCustomersUseCase(store?.id) }
        ) { store, query, tier, customers ->
            // Logic for filtering and mapping goes here
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
