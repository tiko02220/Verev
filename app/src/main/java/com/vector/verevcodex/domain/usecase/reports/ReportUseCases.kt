package com.vector.verevcodex.domain.usecase.reports

import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportExport
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.reports.ReportSection
import com.vector.verevcodex.domain.repository.reports.ReportRepository
import java.time.LocalDate

class ObserveAutoReportSettingsUseCase(private val repository: ReportRepository) {
    operator fun invoke() = repository.observeAutoReportSettings()
}

class SaveAutoReportSettingsUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(settings: ReportAutoSettings) = repository.saveAutoReportSettings(settings)
}

class ExportReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(
        storeId: String?,
        format: ReportFormat,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        includedSections: Set<ReportSection>,
    ): ReportExport =
        repository.exportBusinessReport(storeId, format, dateFrom, dateTo, includedSections)
}
