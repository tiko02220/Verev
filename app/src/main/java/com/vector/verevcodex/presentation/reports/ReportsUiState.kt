package com.vector.verevcodex.presentation.reports

import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportExport

data class ReportsUiState(
    val selectedStoreId: String? = null,
    val selectedStoreName: String = "",
    val autoSettings: ReportAutoSettings = ReportAutoSettings(),
    val latestExport: ReportExport? = null,
    val isExporting: Boolean = false,
    val isSavingAutoSettings: Boolean = false,
    val messageRes: Int? = null,
    val error: String? = null,
)
