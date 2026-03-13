package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.usecase.analytics.ObserveBusinessAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeBusinessAnalyticsUseCase: ObserveBusinessAnalyticsUseCase,
    observeStaffAnalyticsUseCase: ObserveStaffAnalyticsUseCase,
) : ViewModel() {
    private val selectedRange = MutableStateFlow(AnalyticsTimeRange.WEEK)

    val uiState: StateFlow<AnalyticsDashboardUiState> = combine(
        observeSelectedStoreUseCase(),
        selectedRange,
    ) { store, range -> store?.id to range }
        .flatMapLatest { (storeId, range) ->
            combine(
                observeBusinessAnalyticsUseCase(storeId, range),
                observeStaffAnalyticsUseCase(storeId, range),
                selectedRange,
            ) { business, staff, currentRange ->
                AnalyticsDashboardUiState(
                    selectedRange = currentRange,
                    isLoading = false,
                    businessAnalytics = business,
                    staffAnalytics = staff,
                )
            }.onStart {
                emit(
                    AnalyticsDashboardUiState(
                        selectedRange = range,
                        isLoading = true,
                    ),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsDashboardUiState())

    fun updateRange(range: AnalyticsTimeRange) {
        selectedRange.update { range }
    }
}
