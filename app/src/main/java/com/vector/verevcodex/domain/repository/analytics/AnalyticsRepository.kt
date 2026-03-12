package com.vector.verevcodex.domain.repository.analytics

import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.BusinessAnalytics
import com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown
import com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun observeBusinessAnalytics(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.WEEK): Flow<BusinessAnalytics>
    fun observeCustomerAnalytics(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH): Flow<CustomerAnalyticsDrillDown>
    fun observeRevenueAnalytics(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH): Flow<RevenueAnalyticsDrillDown>
    fun observePromotionAnalytics(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH): Flow<PromotionAnalyticsDrillDown>
    fun observeProgramAnalytics(storeId: String? = null, range: AnalyticsTimeRange = AnalyticsTimeRange.MONTH): Flow<ProgramAnalyticsDrillDown>
    fun observeDashboardSnapshot(): Flow<DashboardSnapshot>
}
