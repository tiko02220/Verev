package com.vector.verevcodex.domain.model.reports

data class ReportAutoSettings(
    val enabled: Boolean = false,
    val frequency: ReportAutoFrequency = ReportAutoFrequency.WEEKLY,
    val format: ReportFormat = ReportFormat.DOCX,
    val includeAllStores: Boolean = false,
)
