package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.BusinessAnalytics
import com.vector.verevcodex.domain.model.DashboardSnapshot
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeBusinessAnalytics(storeId: String? = null): Flow<BusinessAnalytics>
    fun observeDashboardSnapshot(): Flow<DashboardSnapshot>
}
