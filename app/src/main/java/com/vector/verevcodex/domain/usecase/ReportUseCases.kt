package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.ReportExport
import com.vector.verevcodex.domain.repository.ReportRepository

class ExportReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(storeId: String?, format: String): ReportExport = repository.exportBusinessReport(storeId, format)
}
