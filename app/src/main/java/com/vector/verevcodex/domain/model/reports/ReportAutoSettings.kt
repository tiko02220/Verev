package com.vector.verevcodex.domain.model.reports

data class ReportAutoSettings(
    val enabled: Boolean = false,
    val frequency: ReportAutoFrequency = ReportAutoFrequency.WEEKLY,
    val format: ReportFormat = ReportFormat.DOCX,
    val includeAllStores: Boolean = false,
    val scheduledTime: String = "09:00",
    val scheduledWeekday: ReportWeekday = ReportWeekday.MONDAY,
    val scheduledMonthDay: Int = 1,
    val recipientEmails: Set<String> = emptySet(),
    val includedSections: Set<ReportSection> = ReportSection.entries.toSet(),
)
