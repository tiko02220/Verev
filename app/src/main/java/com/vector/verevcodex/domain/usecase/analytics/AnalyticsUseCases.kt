package com.vector.verevcodex.domain.usecase.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Note: Use cases are being gradually migrated to individual files for better SRP.
// ObserveProgramAnalyticsUseCase has been moved to its own file.

class ObserveDashboardUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(): Flow<DashboardSnapshot> = repository.observeDashboardSnapshot()
}

class ObserveBusinessAnalyticsUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.WEEK) =
        repository.observeBusinessAnalytics(storeId, range)
}

class ObserveCustomerAnalyticsUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeCustomerAnalytics(storeId, range)
}

class ObserveRevenueAnalyticsUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeRevenueAnalytics(storeId, range)
}

class ObservePromotionAnalyticsUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observePromotionAnalytics(storeId, range)
}
