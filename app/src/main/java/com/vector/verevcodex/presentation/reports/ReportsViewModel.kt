package com.vector.verevcodex.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.usecase.reports.ExportReportUseCase
import com.vector.verevcodex.domain.usecase.reports.ObserveAutoReportSettingsUseCase
import com.vector.verevcodex.domain.usecase.store.ObserveSelectedStoreUseCase
import com.vector.verevcodex.domain.usecase.reports.SaveAutoReportSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeAutoReportSettingsUseCase: ObserveAutoReportSettingsUseCase,
    observeSelectedStoreUseCase: ObserveSelectedStoreUseCase,
    private val saveAutoReportSettingsUseCase: SaveAutoReportSettingsUseCase,
    private val exportReportUseCase: ExportReportUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                observeAutoReportSettingsUseCase(),
                observeSelectedStoreUseCase(),
            ) { autoSettings, selectedStore ->
                autoSettings to selectedStore
            }.collect { (autoSettings, selectedStore) ->
                _uiState.update { state ->
                    state.copy(
                        autoSettings = autoSettings,
                        selectedStoreId = selectedStore?.id,
                        selectedStoreName = selectedStore?.name.orEmpty(),
                    )
                }
            }
        }
    }

    fun setAutoReportsEnabled(enabled: Boolean) {
        saveAutoSettings { copy(enabled = enabled) }
    }

    fun setAutoReportFrequency(frequency: ReportAutoFrequency) {
        saveAutoSettings { copy(frequency = frequency) }
    }

    fun setPreferredFormat(format: ReportFormat) {
        saveAutoSettings { copy(format = format) }
    }

    fun setIncludeAllStores(includeAllStores: Boolean) {
        saveAutoSettings { copy(includeAllStores = includeAllStores) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun export(format: ReportFormat) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null) }
            runCatching {
                exportReportUseCase(
                    storeId = if (_uiState.value.autoSettings.includeAllStores) null else _uiState.value.selectedStoreId,
                    format = format,
                )
            }.onSuccess { report ->
                _uiState.update { state ->
                    state.copy(
                        latestExport = report,
                        isExporting = false,
                        error = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isExporting = false,
                        error = throwable.message,
                    )
                }
            }
        }
    }

    private fun saveAutoSettings(transform: ReportAutoSettings.() -> ReportAutoSettings) {
        viewModelScope.launch {
            val updatedSettings = _uiState.value.autoSettings.transform()
            saveAutoReportSettingsUseCase(updatedSettings)
        }
    }
}
