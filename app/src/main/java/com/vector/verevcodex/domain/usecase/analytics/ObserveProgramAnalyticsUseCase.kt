package com.vector.verevcodex.domain.usecase.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import javax.inject.Inject

class ObserveProgramAnalyticsUseCase @Inject constructor(private val repository: AnalyticsRepository) {
    operator fun invoke(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH) =
        repository.observeProgramAnalytics(storeId, range)
}
