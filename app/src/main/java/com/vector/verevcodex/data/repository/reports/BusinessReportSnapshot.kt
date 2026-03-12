package com.vector.verevcodex.data.repository.reports

import com.vector.verevcodex.domain.model.analytics.BusinessAnalytics
import com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown
import com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown
import com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.model.transactions.Transaction
import java.time.LocalDateTime

internal data class BusinessReportSnapshot(
    val resolvedStoreName: String,
    val generatedAt: LocalDateTime,
    val business: BusinessAnalytics,
    val customer: CustomerAnalyticsDrillDown,
    val revenue: RevenueAnalyticsDrillDown,
    val promotion: PromotionAnalyticsDrillDown,
    val program: ProgramAnalyticsDrillDown,
    val staffAnalytics: List<StaffAnalytics>,
    val transactions: List<Transaction>,
    val activePrograms: Int,
    val activePromotions: Int,
)
