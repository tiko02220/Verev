package com.vector.verevcodex.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.core.UiState
import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboardUseCase: ObserveDashboardUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<DashboardSnapshot>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardSnapshot>> = _uiState.asStateFlow()

    init {
        observeDashboardUseCase()
            .onEach { _uiState.value = UiState.Success(it) }
            .catch { _uiState.value = UiState.Error(it.message ?: "Failed to load dashboard") }
            .launchIn(viewModelScope)
    }
}
