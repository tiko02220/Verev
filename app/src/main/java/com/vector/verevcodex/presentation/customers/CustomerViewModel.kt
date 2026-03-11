package com.vector.verevcodex.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.domain.usecase.AdjustCustomerPointsUseCase
import com.vector.verevcodex.domain.usecase.ObserveCustomersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class CustomerViewModel @Inject constructor(
    observeCustomersUseCase: ObserveCustomersUseCase,
    private val adjustCustomerPointsUseCase: AdjustCustomerPointsUseCase,
) : ViewModel() {
    private val customersState = MutableStateFlow<UiState<List<Customer>>>(UiState.Loading)
    private val searchQuery = MutableStateFlow("")
    private val selectedTier = MutableStateFlow<LoyaltyTier?>(null)

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    init {
        observeCustomersUseCase()
            .map< List<Customer>, UiState<List<Customer>> > { customers ->
                if (customers.isEmpty()) UiState.Empty else UiState.Success(customers)
            }
            .catch { customersState.value = UiState.Error(it.message ?: "Failed to load customers") }
            .onEach { customersState.value = it }
            .launchIn(viewModelScope)

        combine(customersState, searchQuery, selectedTier) { dataState, query, tier ->
            CustomerListUiState(
                dataState = dataState,
                searchQuery = query,
                selectedTier = tier,
                filteredCustomers = filterCustomers(dataState, query, tier),
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value.replace("\n", "")
    }

    fun onTierSelected(tier: LoyaltyTier?) {
        selectedTier.value = tier
    }

    fun rewardLoyaltyBoost(customerId: String) {
        viewModelScope.launch {
            adjustCustomerPointsUseCase(customerId, 20, "Manual loyalty adjustment")
        }
    }

    private fun filterCustomers(
        dataState: UiState<List<Customer>>,
        query: String,
        tier: LoyaltyTier?,
    ): List<Customer> {
        val customers = (dataState as? UiState.Success)?.data.orEmpty()
        return customers.filter { customer ->
            val fullName = listOf(customer.firstName, customer.lastName).joinToString(" ").trim()
            val matchesSearch = query.isBlank() ||
                fullName.contains(query, ignoreCase = true) ||
                customer.email.contains(query, ignoreCase = true) ||
                customer.phoneNumber.contains(query, ignoreCase = true) ||
                customer.loyaltyId.contains(query, ignoreCase = true)
            val matchesTier = tier == null || customer.loyaltyTier == tier
            matchesSearch && matchesTier
        }
    }
}
