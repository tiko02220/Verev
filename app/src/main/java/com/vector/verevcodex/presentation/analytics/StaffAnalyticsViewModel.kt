package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.usecase.staff.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StaffAnalyticsViewModel @Inject constructor(
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    observeStaffAnalyticsUseCase: ObserveStaffAnalyticsUseCase,
) : ViewModel() {
    val uiState: StateFlow<StaffAnalyticsUiState> = observeSelectedStoreUseCase()
        .flatMapLatest { store -> observeStaffAnalyticsUseCase(store?.id) }
        .map { analytics -> StaffAnalyticsUiState(staffAnalytics = analytics.sortedByDescending { it.revenueHandled }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StaffAnalyticsUiState())
}
