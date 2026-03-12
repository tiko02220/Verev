package com.vector.verevcodex.domain.model.reports

import java.time.LocalDateTime

data class ReportExport(
    val fileName: String,
    val format: ReportFormat,
    val summary: String,
    val absolutePath: String,
    val mimeType: String,
    val generatedAt: LocalDateTime,
)
