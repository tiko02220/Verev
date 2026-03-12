package com.vector.verevcodex.data.repository.reports

import com.vector.verevcodex.domain.model.reports.ReportFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object ReportFileMetadata {
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    fun fileName(storeId: String?, format: ReportFormat, generatedAt: LocalDateTime): String {
        val timestamp = generatedAt.format(timestampFormatter)
        val storePart = (storeId ?: "all-stores").replace("[^a-zA-Z0-9-_]".toRegex(), "-")
        return "verev-report-$storePart-$timestamp.${format.name.lowercase()}"
    }

    fun mimeType(format: ReportFormat): String = when (format) {
        ReportFormat.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ReportFormat.XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }
}
