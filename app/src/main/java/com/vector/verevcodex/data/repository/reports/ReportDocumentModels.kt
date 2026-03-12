package com.vector.verevcodex.data.repository.reports

internal data class ReportDocument(
    val title: String,
    val subtitle: String,
    val summary: String,
    val sections: List<ReportSection>,
)

internal data class ReportSection(
    val title: String,
    val rows: List<Pair<String, String>>,
)
