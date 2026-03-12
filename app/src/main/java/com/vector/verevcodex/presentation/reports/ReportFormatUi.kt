package com.vector.verevcodex.presentation.reports

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportFormat

@StringRes
internal fun ReportFormat.titleRes(): Int = when (this) {
    ReportFormat.DOCX -> R.string.merchant_reports_docx_title
    ReportFormat.XLSX -> R.string.merchant_reports_excel_title
}

@StringRes
internal fun ReportAutoFrequency.labelRes(): Int = when (this) {
    ReportAutoFrequency.DAILY -> R.string.merchant_reports_frequency_daily
    ReportAutoFrequency.WEEKLY -> R.string.merchant_reports_frequency_weekly
    ReportAutoFrequency.MONTHLY -> R.string.merchant_reports_frequency_monthly
}
