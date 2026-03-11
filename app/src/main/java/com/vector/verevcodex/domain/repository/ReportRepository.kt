package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.ReportExport

interface ReportRepository {
    suspend fun exportBusinessReport(storeId: String?, format: String): ReportExport
}
