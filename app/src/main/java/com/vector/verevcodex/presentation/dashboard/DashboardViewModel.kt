package com.vector.verevcodex.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.presentation.common.state.UiState
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.model.scan.ScanPreferences
import com.vector.verevcodex.domain.usecase.analytics.ObserveDashboardUseCase
import com.vector.verevcodex.domain.usecase.scan.ObserveScanPreferencesUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboardUseCase: ObserveDashboardUseCase,
    observeScanPreferencesUseCase: ObserveScanPreferencesUseCase,
    observeStoresUseCase: ObserveStoresUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardUseCase()
            .onEach { snapshot ->
                _uiState.value = _uiState.value.copy(snapshotState = UiState.Success(snapshot))
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    snapshotState = UiState.Error(throwable.message ?: "Failed to load dashboard"),
                )
            }
            .launchIn(viewModelScope)

        observeStoresUseCase()
            .drop(1)
            .onEach { stores ->
                if (stores.isEmpty() && _uiState.value.snapshotState !is UiState.Success) {
                    _uiState.value = _uiState.value.copy(snapshotState = UiState.Empty)
                }
            }
            .launchIn(viewModelScope)

        observeScanPreferencesUseCase()
            .onEach { preferences ->
                _uiState.value = _uiState.value.copy(scanPreferences = preferences)
            }
            .launchIn(viewModelScope)
    }
}

data class DashboardUiState(
    val snapshotState: UiState<DashboardSnapshot> = UiState.Loading,
    val scanPreferences: ScanPreferences = ScanPreferences(),
)
