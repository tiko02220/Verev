package com.vector.verevcodex.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vector.verevcodex.domain.model.ReportExport
import com.vector.verevcodex.domain.usecase.ExportReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val exportReportUseCase: ExportReportUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReportExport?>(null)
    val uiState: StateFlow<ReportExport?> = _uiState.asStateFlow()

    fun export(format: String) {
        viewModelScope.launch {
            _uiState.value = exportReportUseCase(null, format)
        }
    }
}
