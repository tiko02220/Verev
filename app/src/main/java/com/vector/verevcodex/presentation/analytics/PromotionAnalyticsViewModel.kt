package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.usecase.analytics.ObservePromotionAnalyticsUseCase
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
class PromotionAnalyticsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observePromotionAnalyticsUseCase: ObservePromotionAnalyticsUseCase,
) : ViewModel() {
    private val selectedRange = MutableStateFlow(AnalyticsTimeRange.MONTH)

    val uiState: StateFlow<PromotionAnalyticsUiState> = combine(
        observeSelectedStoreUseCase(),
        selectedRange,
    ) { store, range -> store?.id to range }
        .flatMapLatest { (storeId, range) ->
            observePromotionAnalyticsUseCase(storeId, range).combine(selectedRange) { analytics, currentRange ->
                PromotionAnalyticsUiState(
                    selectedRange = currentRange,
                    isLoading = false,
                    analytics = analytics,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PromotionAnalyticsUiState())

    fun updateRange(range: AnalyticsTimeRange) {
        selectedRange.update { range }
    }
}
