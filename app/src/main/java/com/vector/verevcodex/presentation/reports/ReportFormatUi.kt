package com.vector.verevcodex.presentation.reports

import androidx.annotation.StringRes
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.reports.ReportSection
import com.vector.verevcodex.domain.model.reports.ReportWeekday

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

@StringRes
internal fun ReportWeekday.labelRes(): Int = when (this) {
    ReportWeekday.MONDAY -> R.string.weekday_monday
    ReportWeekday.TUESDAY -> R.string.weekday_tuesday
    ReportWeekday.WEDNESDAY -> R.string.weekday_wednesday
    ReportWeekday.THURSDAY -> R.string.weekday_thursday
    ReportWeekday.FRIDAY -> R.string.weekday_friday
    ReportWeekday.SATURDAY -> R.string.weekday_saturday
    ReportWeekday.SUNDAY -> R.string.weekday_sunday
}

@StringRes
internal fun ReportSection.labelRes(): Int = when (this) {
    ReportSection.OVERVIEW -> R.string.merchant_reports_section_overview
    ReportSection.REVENUE -> R.string.merchant_reports_section_revenue
    ReportSection.CUSTOMERS -> R.string.merchant_reports_section_customers
    ReportSection.PROMOTIONS -> R.string.merchant_reports_section_promotions
    ReportSection.STAFF -> R.string.merchant_reports_section_staff
    ReportSection.CHARTS -> R.string.merchant_reports_section_charts
}

@StringRes
internal fun ReportSection.descriptionRes(): Int = when (this) {
    ReportSection.OVERVIEW -> R.string.merchant_reports_section_overview_description
    ReportSection.REVENUE -> R.string.merchant_reports_section_revenue_description
    ReportSection.CUSTOMERS -> R.string.merchant_reports_section_customers_description
    ReportSection.PROMOTIONS -> R.string.merchant_reports_section_promotions_description
    ReportSection.STAFF -> R.string.merchant_reports_section_staff_description
    ReportSection.CHARTS -> R.string.merchant_reports_section_charts_description
}
