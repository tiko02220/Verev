package com.vector.verevcodex.data.remote.analytics

import com.vector.verevcodex.data.remote.api.analytics.BusinessAnalyticsViewDto
import com.vector.verevcodex.data.remote.api.analytics.CustomerAnalyticsDrillDownViewDto
import com.vector.verevcodex.data.remote.api.analytics.DashboardCampaignSummaryViewDto
import com.vector.verevcodex.data.remote.api.analytics.DashboardHealthCheckViewDto
import com.vector.verevcodex.data.remote.api.analytics.DashboardHealthSnapshotViewDto
import com.vector.verevcodex.data.remote.api.analytics.DashboardProgramSummaryViewDto
import com.vector.verevcodex.data.remote.api.analytics.DashboardTransactionSummaryViewDto
import com.vector.verevcodex.data.remote.api.analytics.MerchantDashboardSnapshotViewDto
import com.vector.verevcodex.data.remote.api.analytics.ProgramAnalyticsDrillDownViewDto
import com.vector.verevcodex.data.remote.api.analytics.PromotionAnalyticsDrillDownViewDto
import com.vector.verevcodex.data.remote.api.analytics.RevenueAnalyticsDrillDownViewDto
import com.vector.verevcodex.data.remote.api.analytics.StaffAnalyticsViewDto
import com.vector.verevcodex.data.remote.api.analytics.VerevAnalyticsApi
import com.vector.verevcodex.data.remote.api.store.StoreViewDto
import com.vector.verevcodex.data.remote.core.orFalse
import com.vector.verevcodex.data.remote.core.orZero
import com.vector.verevcodex.data.remote.core.parseRemoteInstant
import com.vector.verevcodex.data.remote.core.parseRemoteLocalDate
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.DashboardHealth
import com.vector.verevcodex.domain.model.analytics.DashboardHealthCheck
import com.vector.verevcodex.domain.model.analytics.DashboardHealthCode
import com.vector.verevcodex.domain.model.analytics.DashboardHealthSeverity
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.model.business.BusinessOwner
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.loyalty.RewardProgramConfigurationFactory
import com.vector.verevcodex.domain.model.loyalty.RewardProgramScanAction
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.CampaignTarget
import com.vector.verevcodex.domain.model.promotions.PromotionType
import com.vector.verevcodex.domain.model.promotions.PromotionVisibility
import com.vector.verevcodex.domain.model.common.CampaignSegment
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.transactions.Transaction
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRemoteDataSource @Inject constructor(
    private val api: VerevAnalyticsApi,
) {

    suspend fun dashboardSnapshot(storeId: String?, range: AnalyticsTimeRange): Result<MerchantDashboardSnapshotViewDto> = remoteResult {
        val response = api.dashboardSnapshot(storeId, range.name)
        response.unwrap { it }
    }

    suspend fun business(storeId: String?, range: AnalyticsTimeRange): Result<com.vector.verevcodex.domain.model.analytics.BusinessAnalytics> = remoteResult {
        val response = api.business(storeId, range.name)
        response.unwrap { it.toDomain() }
    }

    suspend fun customers(storeId: String?, range: AnalyticsTimeRange): Result<com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown> = remoteResult {
        val response = api.customers(storeId, range.name)
        response.unwrap { it.toDomain() }
    }

    suspend fun revenue(storeId: String?, range: AnalyticsTimeRange): Result<com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown> = remoteResult {
        val response = api.revenue(storeId, range.name)
        response.unwrap { it.toDomain() }
    }

    suspend fun promotions(storeId: String?, range: AnalyticsTimeRange): Result<com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown> = remoteResult {
        val response = api.promotions(storeId, range.name)
        response.unwrap { it.toDomain() }
    }

    suspend fun programs(storeId: String?, range: AnalyticsTimeRange): Result<com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown> = remoteResult {
        val response = api.programs(storeId, range.name)
        response.unwrap { it.toDomain() }
    }

    suspend fun staff(storeId: String?, range: AnalyticsTimeRange): Result<List<com.vector.verevcodex.domain.model.analytics.StaffAnalytics>> = remoteResult {
        val response = api.staff(storeId, range.name)
        response.unwrap { list -> list.map { it.toDomain() } }
    }

    /** Builds domain [DashboardSnapshot] from API DTO; [ownerId] maps stores, [staffList] is paired with dto.topStaff to build topStaff. */
    fun buildDashboardSnapshot(
        dto: MerchantDashboardSnapshotViewDto,
        owner: BusinessOwner,
        ownerId: String,
        staffList: List<StaffMember>,
    ): DashboardSnapshot {
        val stores = dto.stores.orEmpty().map { it.toStore(ownerId) }
        val selectedStore = dto.scopeStoreId?.let { sid -> stores.firstOrNull { it.id == sid } } ?: stores.firstOrNull()
            ?: error("No store for dashboard")
        val topStaff = dto.topStaff.orEmpty().mapNotNull { saDto ->
            staffList.firstOrNull { it.id == saDto.staffId.orEmpty() }?.let { it to saDto.toDomain() }
        }
        return DashboardSnapshot(
            owner = owner,
            selectedStore = selectedStore,
            stores = stores,
            analytics = dto.analytics?.toDomain()?.copy(scopeStoreId = selectedStore.id)
                ?: com.vector.verevcodex.domain.model.analytics.BusinessAnalytics.empty(selectedStore.id),
            health = dto.health.toDomain(),
            activePrograms = dto.activePrograms.orEmpty().map { it.toDomain() },
            activeCampaigns = dto.activeCampaigns.orEmpty().map { it.toDomain() },
            topStaff = topStaff,
            recentTransactions = dto.recentTransactions.orEmpty().map { it.toDomain() },
        )
    }

}

fun DashboardHealthSnapshotViewDto?.toDomain(): DashboardHealth = DashboardHealth(
    healthy = this?.healthy ?: true,
    checks = this?.checks.orEmpty().map { it.toDomain() },
)

private fun DashboardHealthCheckViewDto.toDomain(): DashboardHealthCheck =
    DashboardHealthCheck(
        code = DashboardHealthCode.entries.firstOrNull { it.name.equals(code.orEmpty(), ignoreCase = true) }
            ?: DashboardHealthCode.UNKNOWN,
        severity = DashboardHealthSeverity.entries.firstOrNull { it.name.equals(severity.orEmpty(), ignoreCase = true) }
            ?: DashboardHealthSeverity.WARNING,
        title = title.orEmpty(),
        message = message.orEmpty(),
        affectedCount = affectedCount.orZero(),
    )

private fun BusinessAnalyticsViewDto.toDomain() = com.vector.verevcodex.domain.model.analytics.BusinessAnalytics(
    id = id.orEmpty(),
    scopeStoreId = scopeStoreId,
    totalCustomers = totalCustomers.orZero(),
    newCustomers = newCustomers.orZero(),
    visitsToday = visitsToday.orZero(),
    visitsInRange = visitsInRange.orZero(),
    totalRevenue = totalRevenue.orZero(),
    averagePurchaseValue = averagePurchaseValue.orZero(),
    rewardRedemptionRate = rewardRedemptionRate.orZero(),
    averagePromotionRoi = averagePromotionRoi.orZero(),
    activePromotions = activePromotions.orZero(),
    retentionRate = retentionRate.orZero(),
    revenueGrowthRate = revenueGrowthRate.orZero(),
    customerGrowthRate = customerGrowthRate.orZero(),
    topCustomerName = topCustomerName.orEmpty(),
    topPromotionName = topPromotionName.orEmpty(),
    revenueTrend = revenueTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    visitTrend = visitTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    newCustomerTrend = newCustomerTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    returningCustomerTrend = returningCustomerTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
)

private fun CustomerAnalyticsDrillDownViewDto.toDomain() = com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown(
    storeId = storeId,
    hasTierAnalytics = hasTierAnalytics.orFalse(),
    totalCustomers = totalCustomers.orZero(),
    newCustomers = newCustomers.orZero(),
    returningCustomers = returningCustomers.orZero(),
    retainedCustomers = retainedCustomers.orZero(),
    inactiveCustomers = inactiveCustomers.orZero(),
    highValueCustomers = highValueCustomers.orZero(),
    averageLifetimeValue = averageLifetimeValue.orZero(),
    topCustomers = topCustomers.orEmpty().map { it.toDomain() },
    tierBreakdown = tierBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    segmentBreakdown = segmentBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    newCustomerTrend = newCustomerTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    returningCustomerTrend = returningCustomerTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    retentionTrend = retentionTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
)

private fun com.vector.verevcodex.data.remote.api.analytics.TopCustomerInsightViewDto.toDomain() =
    com.vector.verevcodex.domain.model.customer.TopCustomerAnalytics(
        customerId = customerId.orEmpty(),
        customerName = customerName.orEmpty(),
        totalSpent = totalSpent.orZero(),
        totalVisits = totalVisits.orZero(),
        loyaltyTier = com.vector.verevcodex.domain.model.common.LoyaltyTier.entries
            .find { it.name.equals(loyaltyTier.orEmpty().replace("-", "_"), true) } ?: com.vector.verevcodex.domain.model.common.LoyaltyTier.BRONZE,
    )

private fun RevenueAnalyticsDrillDownViewDto.toDomain() = com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown(
    storeId = storeId,
    totalRevenue = totalRevenue.orZero(),
    todayRevenue = todayRevenue.orZero(),
    averageOrderValue = averageOrderValue.orZero(),
    transactionCount = transactionCount.orZero(),
    redeemedPointsValue = redeemedPointsValue.orZero(),
    revenueGrowthRate = revenueGrowthRate.orZero(),
    revenueTrend = revenueTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    timeBucketTrend = timeBucketTrend.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsPoint(it.label.orEmpty(), it.value.orZero().toFloat()) },
    sourceBreakdown = sourceBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
)

private fun PromotionAnalyticsDrillDownViewDto.toDomain() = com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown(
    storeId = storeId,
    totalPromotions = totalPromotions.orZero(),
    activePromotions = activePromotions.orZero(),
    scheduledPromotions = scheduledPromotions.orZero(),
    expiredPromotions = expiredPromotions.orZero(),
    paymentPromotions = paymentPromotions.orZero(),
    averageRoiScore = averageRoiScore.orZero(),
    typeBreakdown = typeBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    statusBreakdown = statusBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    topPromotions = topPromotions.orEmpty().map { it.toDomain() },
)

private fun com.vector.verevcodex.data.remote.api.analytics.PromotionPerformanceViewDto.toDomain() =
    com.vector.verevcodex.domain.model.promotions.PromotionPerformance(
        promotionId = promotionId.orEmpty(),
        name = name.orEmpty(),
        type = com.vector.verevcodex.domain.model.promotions.PromotionType.entries
            .find { it.name.equals(type.orEmpty().replace("-", "_").replace(" ", "_"), true) } ?: com.vector.verevcodex.domain.model.promotions.PromotionType.PERCENT_DISCOUNT,
        paymentFlowEnabled = paymentFlowEnabled.orFalse(),
        active = active.orFalse(),
        estimatedUsageCount = estimatedUsageCount.orZero(),
        revenueImpact = revenueImpact.orZero(),
        roiScore = roiScore.orZero(),
    )

private fun ProgramAnalyticsDrillDownViewDto.toDomain() = com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown(
    storeId = storeId,
    totalPrograms = totalPrograms.orZero(),
    activePrograms = activePrograms.orZero(),
    memberParticipationRate = memberParticipationRate.orZero(),
    redemptionEfficiency = redemptionEfficiency.orZero(),
    typeBreakdown = typeBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    rewardUsageBreakdown = rewardUsageBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    scanActionBreakdown = scanActionBreakdown.orEmpty().map { com.vector.verevcodex.domain.model.analytics.AnalyticsSegment(it.label.orEmpty(), it.value.orZero()) },
    topPrograms = topPrograms.orEmpty().map { it.toDomain() },
)

private fun com.vector.verevcodex.data.remote.api.analytics.ProgramPerformanceViewDto.toDomain() =
    com.vector.verevcodex.domain.model.loyalty.ProgramPerformance(
        programId = programId.orEmpty(),
        name = name.orEmpty(),
        type = com.vector.verevcodex.domain.model.common.LoyaltyProgramType.entries
            .find { it.name.equals(type.orEmpty().replace("-", "_").replace(" ", "_"), true) } ?: com.vector.verevcodex.domain.model.common.LoyaltyProgramType.POINTS,
        active = active.orFalse(),
        scanActionsEnabled = scanActionsEnabled.orZero(),
        memberCount = memberCount.orZero(),
        redemptionRate = redemptionRate.orZero(),
    )

private fun StaffAnalyticsViewDto.toDomain() = com.vector.verevcodex.domain.model.analytics.StaffAnalytics(
    id = id.orEmpty(),
    staffId = staffId.orEmpty(),
    staffName = staffName.orEmpty(),
    storeId = storeId.orEmpty(),
    transactionsProcessed = transactionsProcessed.orZero(),
    revenueHandled = revenueHandled.orZero(),
    customersServed = customersServed.orZero(),
    rewardsRedeemed = rewardsRedeemed.orZero(),
    averageTransactionValue = averageTransactionValue.orZero(),
)

private fun StoreViewDto.toStore(ownerId: String) = Store(
    id = id.orEmpty(),
    ownerId = ownerId,
    name = name.orEmpty(),
    address = address.orEmpty(),
    contactInfo = contactInfo.orEmpty(),
    category = category.orEmpty(),
    workingHours = workingHours.orEmpty(),
    logoUrl = logoUrl.orEmpty(),
    primaryColor = primaryColor.orEmpty().ifEmpty { "#0C3B2E" },
    secondaryColor = secondaryColor.orEmpty().ifEmpty { "#FFBA00" },
    active = active.orFalse(),
)

private fun DashboardProgramSummaryViewDto.toDomain(): RewardProgram {
    val progType = LoyaltyProgramType.entries.find { it.name.equals(type.orEmpty().replace("-", "_"), true) } ?: LoyaltyProgramType.POINTS
    val config = RewardProgramConfigurationFactory.defaultFor(progType, active.orFalse())
    val mappedScanActions = scanActions.orEmpty().mapNotNull { s ->
        RewardProgramScanAction.entries.find { it.name.equals(s.replace("-", "_"), true) }
    }.toSet()
    return RewardProgram(
        id = id.orEmpty(),
        storeId = storeId.orEmpty(),
        name = name.orEmpty(),
        description = "",
        type = progType,
        rulesSummary = "",
        active = active.orFalse(),
        autoScheduleEnabled = false,
        scheduleStartDate = null,
        scheduleEndDate = null,
        annualRepeatEnabled = false,
        configuration = config.copy(scanActions = if (mappedScanActions.isEmpty()) config.scanActions else mappedScanActions),
    )
}

private fun DashboardCampaignSummaryViewDto.toDomain(): Campaign = Campaign(
    id = id.orEmpty(),
    storeId = storeId.orEmpty(),
    name = name.orEmpty(),
    description = "",
    imageUri = null,
    startDate = parseRemoteLocalDate(startDate),
    endDate = parseRemoteLocalDate(endDate),
    promotionType = PromotionType.entries.find { it.name.equals(promotionType.orEmpty().replace("-", "_").replace(" ", "_"), true) } ?: PromotionType.PERCENT_DISCOUNT,
    promotionValue = 0.0,
    minimumPurchaseAmount = 0.0,
    usageLimit = 0,
    promoCode = null,
    visibility = PromotionVisibility.BUSINESS_ONLY,
    boostLevel = null,
    paymentFlowEnabled = false,
    active = active.orFalse(),
    target = CampaignTarget(id = "${id.orEmpty()}-target", campaignId = id.orEmpty(), segment = CampaignSegment.ALL_CUSTOMERS, description = ""),
)

private fun DashboardTransactionSummaryViewDto.toDomain(): Transaction {
    val instant = parseRemoteInstant(occurredAt)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return Transaction(
        id = id.orEmpty(),
        customerId = customerId.orEmpty(),
        storeId = storeId.orEmpty(),
        staffId = "",
        amount = amount.orZero(),
        pointsEarned = pointsEarned.orZero(),
        pointsRedeemed = pointsRedeemed.orZero(),
        timestamp = dateTime,
        metadata = "",
        countsAsVisit = true,
        items = emptyList(),
    )
}
