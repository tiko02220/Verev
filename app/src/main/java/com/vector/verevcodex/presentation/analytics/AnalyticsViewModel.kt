package com.vector.verevcodex.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.BusinessAnalytics
import com.vector.verevcodex.domain.model.StaffAnalytics
import com.vector.verevcodex.domain.usecase.ObserveStaffAnalyticsUseCase
import com.vector.verevcodex.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    analyticsRepository: AnalyticsRepository,
    observeStaffAnalyticsUseCase: ObserveStaffAnalyticsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        combine(analyticsRepository.observeBusinessAnalytics(null), observeStaffAnalyticsUseCase()) { business, staff ->
            AnalyticsUiState(business, staff)
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }
}

data class AnalyticsUiState(
    val businessAnalytics: BusinessAnalytics? = null,
    val staffAnalytics: List<StaffAnalytics> = emptyList(),
)
