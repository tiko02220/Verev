package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsByStoreUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerViewModel @Inject constructor(
    observeCustomersUseCase: ObserveCustomersUseCase,
    observeCustomerRelationsByStoreUseCase: ObserveCustomerRelationsByStoreUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
) : ViewModel() {
    private val customersState = MutableStateFlow<UiState<List<CustomerListCardUi>>>(UiState.Loading)
    private val errorRes = MutableStateFlow<Int?>(null)
    private val searchQuery = MutableStateFlow("")
    private val selectedTier = MutableStateFlow<LoyaltyTier?>(null)

    private val _uiState = MutableStateFlow(CustomerListUiState())
    internal val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeSelectedStoreUseCase(),
            observeCustomersUseCase(),
            observeSelectedStoreUseCase().map { it?.id }.flatMapLatest { storeId ->
                if (storeId == null) kotlinx.coroutines.flow.flowOf(emptyList()) else observeCustomerRelationsByStoreUseCase(storeId)
            },
        ) { selectedStore, customers, relations ->
            val storeId = selectedStore?.id
            val scopedCustomers = if (storeId == null) customers else customers.filter { it.favoriteStoreId == storeId || it.favoriteStoreId == null }
            val cards = mapCustomerListCards(scopedCustomers, relations)
            if (cards.isEmpty()) UiState.Empty else UiState.Success(cards)
        }
            .catch {
                customersState.value = UiState.Empty
                errorRes.value = R.string.merchant_customers_error_subtitle
            }
            .onEach {
                errorRes.value = null
                customersState.value = it
            }
            .launchIn(viewModelScope)

        combine(customersState, errorRes, searchQuery, selectedTier, observeSelectedStoreUseCase()) { dataState, errorRes, query, tier, selectedStore ->
            CustomerListUiState(
                dataState = dataState,
                errorRes = errorRes,
                searchQuery = query,
                selectedTier = tier,
                selectedStoreName = selectedStore?.name.orEmpty(),
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
}
