package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow

class ObserveDashboardUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(): Flow<DashboardSnapshot> = repository.observeDashboardSnapshot()
}
