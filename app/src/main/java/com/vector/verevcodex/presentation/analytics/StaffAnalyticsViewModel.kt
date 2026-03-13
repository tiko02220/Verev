package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StaffAnalyticsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeStaffAnalyticsUseCase: ObserveStaffAnalyticsUseCase,
) : ViewModel() {
    private val selectedRange = MutableStateFlow(AnalyticsTimeRange.MONTH)

    val uiState: StateFlow<StaffAnalyticsUiState> = combine(
        observeSelectedStoreUseCase(),
        selectedRange,
    ) { store, range -> store?.id to range }
        .flatMapLatest { (storeId, range) ->
            observeStaffAnalyticsUseCase(storeId, range).combine(selectedRange) { analytics, currentRange ->
                StaffAnalyticsUiState(
                    isLoading = false,
                    selectedRange = currentRange,
                    staffAnalytics = analytics.sortedByDescending { it.revenueHandled },
                )
            }.onStart {
                emit(
                    StaffAnalyticsUiState(
                        selectedRange = range,
                        isLoading = true,
                    ),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StaffAnalyticsUiState())

    fun updateRange(range: AnalyticsTimeRange) {
        selectedRange.update { range }
    }
}
