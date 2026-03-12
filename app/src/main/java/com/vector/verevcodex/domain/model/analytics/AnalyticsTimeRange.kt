package com.vector.verevcodex.domain.model.analytics

import java.time.LocalDate

enum class AnalyticsTimeRange {
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
}

fun AnalyticsTimeRange.startDateFrom(endDate: LocalDate): LocalDate = when (this) {
    AnalyticsTimeRange.WEEK -> endDate.minusDays(6)
    AnalyticsTimeRange.MONTH -> endDate.minusDays(29)
    AnalyticsTimeRange.QUARTER -> endDate.minusDays(89)
    AnalyticsTimeRange.YEAR -> endDate.minusDays(364)
}
