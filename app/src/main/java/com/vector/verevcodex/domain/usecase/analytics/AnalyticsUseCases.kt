package com.vector.verevcodex.domain.usecase.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import kotlinx.coroutines.flow.Flow

class ObserveDashboardUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(): Flow<DashboardSnapshot> = repository.observeDashboardSnapshot()
}

class ObserveBusinessAnalyticsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.WEEK) =
        repository.observeBusinessAnalytics(storeId, range)
}

class ObserveCustomerAnalyticsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeCustomerAnalytics(storeId, range)
}

class ObserveRevenueAnalyticsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeRevenueAnalytics(storeId, range)
}

class ObservePromotionAnalyticsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observePromotionAnalytics(storeId, range)
}

class ObserveProgramAnalyticsUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeProgramAnalytics(storeId, range)
}
