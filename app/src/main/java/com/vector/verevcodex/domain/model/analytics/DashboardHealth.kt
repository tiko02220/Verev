package com.vector.verevcodex.domain.model.analytics

enum class DashboardHealthSeverity {
    WARNING,
    ACTION_REQUIRED,
}

enum class DashboardHealthCode {
    PROGRAMS_INACTIVE,
    REPORTS_FAILING,
    NOTIFICATIONS_DISABLED,
    STAFF_NOT_ACTIVATED,
    STAFF_WITHOUT_PIN,
    UNKNOWN,
}

data class DashboardHealthCheck(
    val code: DashboardHealthCode,
    val severity: DashboardHealthSeverity,
    val title: String,
    val message: String,
    val affectedCount: Int,
)

data class DashboardHealth(
    val healthy: Boolean = true,
    val checks: List<DashboardHealthCheck> = emptyList(),
)
