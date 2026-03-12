package com.vector.verevcodex.domain.repository.reports

import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportExport
import com.vector.verevcodex.domain.model.reports.ReportFormat
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun observeAutoReportSettings(): Flow<ReportAutoSettings>
    suspend fun saveAutoReportSettings(settings: ReportAutoSettings)
    suspend fun exportBusinessReport(storeId: String?, format: ReportFormat): ReportExport
}
