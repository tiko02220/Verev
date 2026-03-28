package com.vector.verevcodex.presentation.customers

import com.vector.verevcodex.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomersUseCase
import com.vector.verevcodex.domain.usecase.customer.ObserveCustomerRelationsByStoreUseCase
import com.vector.verevcodex.domain.usecase.loyalty.ObserveProgramsUseCase
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
    observeProgramsUseCase: ObserveProgramsUseCase,
) : ViewModel() {
    private val customersState = MutableStateFlow<UiState<List<CustomerListCardUi>>>(UiState.Loading)
    private val errorRes = MutableStateFlow<Int?>(null)
    private val searchQuery = MutableStateFlow("")
    private val selectedTier = MutableStateFlow<LoyaltyTier?>(null)
    private val selectedSort = MutableStateFlow(CustomerListSortOption.RECENT_ACTIVITY)
    private val hasActiveTierProgram = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(CustomerListUiState())
    internal val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    init {
        val selectedStoreFlow = observeSelectedStoreUseCase()

        combine(
            selectedStoreFlow,
            selectedStoreFlow.map { it?.id }.flatMapLatest { storeId ->
                observeCustomersUseCase(storeId)
            },
            selectedStoreFlow.map { it?.id }.flatMapLatest { storeId ->
                if (storeId == null) kotlinx.coroutines.flow.flowOf(emptyList()) else observeCustomerRelationsByStoreUseCase(storeId)
            },
            selectedStoreFlow.map { it?.id }.flatMapLatest { storeId ->
                observeProgramsUseCase(storeId)
            },
        ) { selectedStore, customers, relations, programs ->
            val storeId = selectedStore?.id
            val relatedCustomerIds = relations.mapTo(mutableSetOf()) { it.customerId }
            val activeTierProgram = programs.firstOrNull { program ->
                program.active && program.configuration.tierTrackingEnabled
            }
            val hasActiveTierProgram = activeTierProgram != null
            val scopedCustomers = if (storeId == null) {
                customers
            } else {
                customers.filter { customer ->
                    customer.id in relatedCustomerIds || customer.favoriteStoreId == storeId
                }
            }
            val cards = mapCustomerListCards(
                customers = scopedCustomers,
                relations = relations,
                tierRule = activeTierProgram?.configuration?.tierRule,
            )
            CustomerListDataState(
                customers = if (cards.isEmpty()) UiState.Empty else UiState.Success(cards),
                hasActiveTierProgram = hasActiveTierProgram,
            )
        }
            .catch {
                customersState.value = UiState.Empty
                errorRes.value = R.string.merchant_customers_error_subtitle
            }
            .onEach {
                errorRes.value = null
                customersState.value = it.customers
                hasActiveTierProgram.value = it.hasActiveTierProgram
            }
            .launchIn(viewModelScope)

        combine(
            combine(
                combine(customersState, errorRes, searchQuery, selectedTier, selectedSort) { dataState, errorRes, query, tier, sort ->
                    CustomerListUiState(
                        dataState = dataState,
                        errorRes = errorRes,
                        searchQuery = query,
                        selectedTier = tier,
                        selectedSort = sort,
                    )
                },
                hasActiveTierProgram,
            ) { partialState, hasActiveTierProgram ->
                CustomerListUiState(
                    dataState = partialState.dataState,
                    errorRes = partialState.errorRes,
                    searchQuery = partialState.searchQuery,
                    selectedTier = partialState.selectedTier,
                    selectedSort = partialState.selectedSort,
                    hasActiveTierProgram = hasActiveTierProgram,
                    filteredCustomers = filterCustomers(
                        dataState = partialState.dataState,
                        query = partialState.searchQuery,
                        tier = if (hasActiveTierProgram) partialState.selectedTier else null,
                        sort = partialState.selectedSort,
                    ),
                )
            },
            selectedStoreFlow,
        ) { partialState, selectedStore ->
            partialState.copy(selectedStoreName = selectedStore?.name.orEmpty())
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value.replace("\n", "")
    }

    fun onTierSelected(tier: LoyaltyTier?) {
        selectedTier.value = tier
    }

    fun onSortSelected(sort: CustomerListSortOption) {
        selectedSort.value = sort
    }
}

private data class CustomerListDataState(
    val customers: UiState<List<CustomerListCardUi>>,
    val hasActiveTierProgram: Boolean,
)
