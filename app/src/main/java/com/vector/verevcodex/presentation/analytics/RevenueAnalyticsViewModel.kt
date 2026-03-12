package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.usecase.analytics.ObserveRevenueAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class RevenueAnalyticsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeRevenueAnalyticsUseCase: ObserveRevenueAnalyticsUseCase,
) : ViewModel() {
    private val selectedRange = MutableStateFlow(AnalyticsTimeRange.MONTH)

    val uiState: StateFlow<RevenueAnalyticsUiState> = combine(
        observeSelectedStoreUseCase(),
        selectedRange,
    ) { store, range -> store?.id to range }
        .flatMapLatest { (storeId, range) ->
            observeRevenueAnalyticsUseCase(storeId, range).combine(selectedRange) { analytics, currentRange ->
                RevenueAnalyticsUiState(
                    selectedRange = currentRange,
                    isLoading = false,
                    analytics = analytics,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RevenueAnalyticsUiState())

    fun updateRange(range: AnalyticsTimeRange) {
        selectedRange.update { range }
    }
}
