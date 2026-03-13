package com.vector.verevcodex.domain.model.analytics

import java.time.LocalDate
import java.time.DayOfWeek

enum class AnalyticsTimeRange {
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
}

fun AnalyticsTimeRange.startDateFrom(endDate: LocalDate): LocalDate = when (this) {
    AnalyticsTimeRange.WEEK -> endDate.minusDays(((endDate.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7).toLong())
    AnalyticsTimeRange.MONTH -> endDate.minusDays(29)
    AnalyticsTimeRange.QUARTER -> endDate.minusDays(89)
    AnalyticsTimeRange.YEAR -> endDate.minusDays(364)
}
