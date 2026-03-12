package com.vector.verevcodex.data.repository.analytics

import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.domain.model.analytics.AnalyticsPoint
import com.vector.verevcodex.domain.model.analytics.AnalyticsSegment
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange.MONTH
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange.QUARTER
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange.WEEK
import com.vector.verevcodex.domain.model.analytics.AnalyticsTimeRange.YEAR
import com.vector.verevcodex.domain.model.analytics.BusinessAnalytics
import com.vector.verevcodex.domain.model.analytics.DashboardSnapshot
import com.vector.verevcodex.domain.model.analytics.RevenueAnalyticsDrillDown
import com.vector.verevcodex.domain.model.analytics.StaffAnalytics
import com.vector.verevcodex.domain.model.analytics.startDateFrom
import com.vector.verevcodex.domain.model.auth.AuthSession
import com.vector.verevcodex.domain.model.business.BusinessOwner
import com.vector.verevcodex.domain.model.business.StaffMember
import com.vector.verevcodex.domain.model.business.Store
import com.vector.verevcodex.domain.model.common.LoyaltyProgramType
import com.vector.verevcodex.domain.model.common.LoyaltyTier
import com.vector.verevcodex.domain.model.common.StaffRole
import com.vector.verevcodex.domain.model.customer.Customer
import com.vector.verevcodex.domain.model.customer.CustomerAnalyticsDrillDown
import com.vector.verevcodex.domain.model.customer.TopCustomerAnalytics
import com.vector.verevcodex.domain.model.loyalty.ProgramAnalyticsDrillDown
import com.vector.verevcodex.domain.model.loyalty.ProgramPerformance
import com.vector.verevcodex.domain.model.loyalty.RewardProgram
import com.vector.verevcodex.domain.model.promotions.Campaign
import com.vector.verevcodex.domain.model.promotions.PromotionAnalyticsDrillDown
import com.vector.verevcodex.domain.model.promotions.PromotionPerformance
import com.vector.verevcodex.domain.model.transactions.Transaction
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val storeRepository: StoreRepository,
    private val staffRepository: StaffRepository,
    private val loyaltyRepository: LoyaltyRepository,
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository,
) : AnalyticsRepository {
    override fun observeBusinessAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<BusinessAnalytics> = combine(
        database.customerDao().observeCustomers(storeId),
        database.transactionDao().observeTransactions(storeId),
        loyaltyRepository.observeCampaigns(storeId),
    ) { customerEntities, transactionEntities, promotions ->
        val today = LocalDate.now()
        val customers = customerEntities.map { it.toDomain() }
        val transactions = transactionEntities.map { it.toDomain(emptyList()) }
        val rangeWindow = range.windowEndingOn(today)
        val previousWindow = range.previousWindowBefore(today)
        val rangeTransactions = transactions.inDateRange(rangeWindow.start, rangeWindow.end)
        val previousTransactions = transactions.inDateRange(previousWindow.start, previousWindow.end)
        val totalRevenue = rangeTransactions.sumOf { it.amount }
        val previousRevenue = previousTransactions.sumOf { it.amount }
        val newCustomersInRange = customers.count { it.enrolledDate in rangeWindow.start..rangeWindow.end }
        val previousNewCustomers = customers.count { it.enrolledDate in previousWindow.start..previousWindow.end }

        BusinessAnalytics(
            id = storeId ?: "all-stores",
            scopeStoreId = storeId,
            totalCustomers = customers.distinctBy(Customer::id).size,
            newCustomers = newCustomersInRange,
            visitsToday = transactions.count { it.timestamp.toLocalDate() == today },
            totalRevenue = totalRevenue,
            averagePurchaseValue = rangeTransactions.averageAmount(),
            rewardRedemptionRate = rangeTransactions.redemptionRate(),
            retentionRate = customers.retentionRate(),
            revenueGrowthRate = growthRate(totalRevenue, previousRevenue),
            customerGrowthRate = growthRate(newCustomersInRange.toDouble(), previousNewCustomers.toDouble()),
            topCustomerName = customers.maxByOrNull(Customer::totalSpent)?.displayName().orEmpty(),
            topPromotionName = topPromotionName(promotions, rangeTransactions),
            revenueTrend = buildPeriodRevenueTrend(rangeTransactions, range),
            visitTrend = buildPeriodVisitTrend(rangeTransactions, range),
        )
    }

    override fun observeCustomerAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<CustomerAnalyticsDrillDown> = combine(
        database.customerDao().observeCustomers(storeId),
        database.transactionDao().observeTransactions(storeId),
    ) { customerEntities, transactionEntities ->
        val today = LocalDate.now()
        val customers = customerEntities.map { it.toDomain() }
        val transactions = transactionEntities.map { it.toDomain(emptyList()) }
        val rangeWindow = range.windowEndingOn(today)
        val previousWindow = range.previousWindowBefore(today)
        val rangeTransactions = transactions.inDateRange(rangeWindow.start, rangeWindow.end)
        val currentVisitors = rangeTransactions.map(Transaction::customerId).toSet()
        val previousVisitors = transactions.inDateRange(previousWindow.start, previousWindow.end).map(Transaction::customerId).toSet()

        CustomerAnalyticsDrillDown(
            storeId = storeId,
            totalCustomers = customers.size,
            newCustomers = customers.count { it.enrolledDate in rangeWindow.start..rangeWindow.end },
            returningCustomers = customers.count { it.totalVisits > 1 },
            retainedCustomers = customers.count { it.totalVisits >= 5 },
            inactiveCustomers = customers.count { it.lastVisit == null || it.lastVisit.toLocalDate().isBefore(today.minusDays(45)) },
            highValueCustomers = customers.count { it.totalSpent >= highValueThreshold(customers) },
            averageLifetimeValue = customers.averageLifetimeValue(),
            topCustomers = customers.sortedByDescending(Customer::totalSpent)
                .take(5)
                .map { customer ->
                    TopCustomerAnalytics(
                        customerId = customer.id,
                        customerName = customer.displayName(),
                        totalSpent = customer.totalSpent,
                        totalVisits = customer.totalVisits,
                        loyaltyTier = customer.loyaltyTier,
                    )
                },
            tierBreakdown = customerTierSegments(customers),
            segmentBreakdown = listOf(
                AnalyticsSegment("High value", customers.count { it.totalSpent >= highValueThreshold(customers) }),
                AnalyticsSegment("At risk", customers.count { it.lastVisit == null || it.lastVisit.toLocalDate().isBefore(today.minusDays(30)) }),
                AnalyticsSegment("Inactive", customers.count { it.lastVisit == null || it.lastVisit.toLocalDate().isBefore(today.minusDays(45)) }),
                AnalyticsSegment("Returning", customers.count { it.totalVisits > 1 }),
            ).filter { it.value > 0 },
            activityTrend = buildCustomerActivityTrend(customers, rangeTransactions, range),
            retentionTrend = buildRetentionTrend(range, currentVisitors, previousVisitors),
        )
    }

    override fun observeRevenueAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<RevenueAnalyticsDrillDown> =
        database.transactionDao().observeTransactions(storeId).map { transactionEntities ->
            val today = LocalDate.now()
            val transactions = transactionEntities.map { it.toDomain(emptyList()) }
            val rangeWindow = range.windowEndingOn(today)
            val previousWindow = range.previousWindowBefore(today)
            val rangeTransactions = transactions.inDateRange(rangeWindow.start, rangeWindow.end)
            val previousTransactions = transactions.inDateRange(previousWindow.start, previousWindow.end)
            RevenueAnalyticsDrillDown(
                storeId = storeId,
                totalRevenue = rangeTransactions.sumOf(Transaction::amount),
                todayRevenue = transactions.filter { it.timestamp.toLocalDate() == today }.sumOf(Transaction::amount),
                averageOrderValue = rangeTransactions.averageAmount(),
                transactionCount = rangeTransactions.size,
                redeemedPointsValue = rangeTransactions.sumOf { it.pointsRedeemed.toDouble() },
                revenueGrowthRate = growthRate(
                    rangeTransactions.sumOf(Transaction::amount),
                    previousTransactions.sumOf(Transaction::amount),
                ),
                revenueTrend = buildPeriodRevenueTrend(rangeTransactions, range),
                timeBucketTrend = buildTimeBucketTrend(rangeTransactions),
                sourceBreakdown = revenueSourceBreakdown(rangeTransactions),
            )
        }

    override fun observePromotionAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<PromotionAnalyticsDrillDown> = combine(
        loyaltyRepository.observeCampaigns(storeId),
        transactionRepository.observeTransactions(storeId),
    ) { promotions, transactions ->
        val today = LocalDate.now()
        val rangeWindow = range.windowEndingOn(today)
        val rangeTransactions = transactions.inDateRange(rangeWindow.start, rangeWindow.end)
        val scopedPromotions = promotions.filter { it.endDate >= rangeWindow.start && it.startDate <= rangeWindow.end }
        val topPromotions = scopedPromotions.map { promotion ->
            val usageCount = estimatePromotionUsage(promotion, rangeTransactions)
            val revenueImpact = estimatePromotionRevenueImpact(promotion, rangeTransactions)
            val roiScore = if (promotion.promotionValue <= 0.0) revenueImpact else revenueImpact / (promotion.promotionValue * usageCount.coerceAtLeast(1))
            PromotionPerformance(
                promotionId = promotion.id,
                name = promotion.name,
                type = promotion.promotionType,
                paymentFlowEnabled = promotion.paymentFlowEnabled,
                active = promotion.active && !today.isBefore(promotion.startDate) && !today.isAfter(promotion.endDate),
                estimatedUsageCount = usageCount,
                revenueImpact = revenueImpact,
                roiScore = roiScore,
            )
        }.sortedByDescending(PromotionPerformance::roiScore).take(6)

        PromotionAnalyticsDrillDown(
            storeId = storeId,
            totalPromotions = scopedPromotions.size,
            activePromotions = scopedPromotions.count { it.active && !today.isBefore(it.startDate) && !today.isAfter(it.endDate) },
            scheduledPromotions = scopedPromotions.count { today.isBefore(it.startDate) },
            expiredPromotions = scopedPromotions.count { today.isAfter(it.endDate) },
            paymentPromotions = scopedPromotions.count { it.paymentFlowEnabled },
            averageRoiScore = topPromotions.map(PromotionPerformance::roiScore).average().takeIf { !it.isNaN() } ?: 0.0,
            typeBreakdown = scopedPromotions.groupBy { it.promotionType }
                .map { (type, items) -> AnalyticsSegment(type.name.humanize(), items.size) }
                .sortedByDescending(AnalyticsSegment::value),
            statusBreakdown = listOf(
                AnalyticsSegment("Active", scopedPromotions.count { it.active && !today.isBefore(it.startDate) && !today.isAfter(it.endDate) }),
                AnalyticsSegment("Scheduled", scopedPromotions.count { today.isBefore(it.startDate) }),
                AnalyticsSegment("Expired", scopedPromotions.count { today.isAfter(it.endDate) }),
                AnalyticsSegment("Payment", scopedPromotions.count { it.paymentFlowEnabled }),
            ).filter { it.value > 0 },
            topPromotions = topPromotions,
        )
    }

    override fun observeProgramAnalytics(storeId: String?, range: AnalyticsTimeRange): Flow<ProgramAnalyticsDrillDown> = combine(
        loyaltyRepository.observePrograms(storeId),
        transactionRepository.observeTransactions(storeId),
    ) { programs, transactions ->
        val today = LocalDate.now()
        val rangeTransactions = transactions.inDateRange(range.startDateFrom(today), today)
        val activePrograms = programs.count { it.active }
        val participants = rangeTransactions.map(Transaction::customerId).distinct().size
        ProgramAnalyticsDrillDown(
            storeId = storeId,
            totalPrograms = programs.size,
            activePrograms = activePrograms,
            memberParticipationRate = if (rangeTransactions.isEmpty()) 0.0 else participants.toDouble() / rangeTransactions.size.coerceAtLeast(1),
            redemptionEfficiency = if (rangeTransactions.sumOf { it.pointsEarned } == 0) 0.0 else rangeTransactions.sumOf { it.pointsRedeemed }.toDouble() / rangeTransactions.sumOf { it.pointsEarned },
            typeBreakdown = programs.groupBy { it.type }
                .map { (type, items) -> AnalyticsSegment(type.name.humanize(), items.size) }
                .sortedByDescending(AnalyticsSegment::value),
            rewardUsageBreakdown = rewardUsageBreakdown(rangeTransactions),
            scanActionBreakdown = programs.flatMap { it.configuration.scanActions }
                .groupBy { it.name.humanize() }
                .map { (label, items) -> AnalyticsSegment(label, items.size) }
                .sortedByDescending(AnalyticsSegment::value),
            topPrograms = programs.sortedWith(compareByDescending<RewardProgram> { it.active }.thenBy { it.name })
                .take(6)
                .map { program ->
                    ProgramPerformance(
                        programId = program.id,
                        name = program.name,
                        type = program.type,
                        active = program.active,
                        scanActionsEnabled = program.configuration.scanActions.size,
                        memberCount = estimateProgramMemberCount(program, rangeTransactions),
                        redemptionRate = estimateProgramRedemptionRate(program, rangeTransactions),
                    )
                },
        )
    }

    override fun observeDashboardSnapshot(): Flow<DashboardSnapshot> {
        val storeSelectionFlow: Flow<Pair<Store?, List<Store>>> = combine(
            storeRepository.observeSelectedStore(),
            storeRepository.observeStores(),
        ) { selectedStore, stores ->
            selectedStore to stores
        }

        val commerceFlow = storeRepository.observeSelectedStore()
            .map { it?.id }
            .flatMapLatest { selectedStoreId ->
                combine(
                    observeBusinessAnalytics(selectedStoreId, WEEK),
                    loyaltyRepository.observePrograms(selectedStoreId),
                    loyaltyRepository.observeCampaigns(selectedStoreId),
                    transactionRepository.observeTransactions(selectedStoreId),
                ) { analytics, programs, campaigns, transactions ->
                    DashboardCommerceBundle(analytics, programs, campaigns, transactions)
                }
            }

        val staffFlow = storeRepository.observeSelectedStore()
            .map { it?.id }
            .flatMapLatest { selectedStoreId ->
                combine(
                    staffRepository.observeStaff(selectedStoreId),
                    staffRepository.observeStaffAnalytics(selectedStoreId),
                ) { staff, staffAnalytics ->
                    DashboardStaffBundle(staff, staffAnalytics)
                }
            }

        return combine(
            authRepository.observeSession(),
            storeSelectionFlow,
            commerceFlow,
            staffFlow,
        ) { session, storeSelection, commerce, staffBundle ->
            val stores = storeSelection.second
            val currentStore = storeSelection.first ?: stores.firstOrNull() ?: return@combine null
            val owner = resolveOwner(session)
            val topStaff = staffBundle.analytics
                .sortedByDescending(StaffAnalytics::revenueHandled)
                .take(3)
                .mapNotNull { analyticsItem ->
                    staffBundle.staff.firstOrNull { it.id == analyticsItem.staffId }?.let { it to analyticsItem }
                }

            DashboardSnapshot(
                owner = owner,
                selectedStore = currentStore,
                stores = stores,
                analytics = commerce.analytics.copy(scopeStoreId = currentStore.id),
                activePrograms = commerce.programs.filter { it.active && it.storeId == currentStore.id },
                activeCampaigns = commerce.campaigns.filter { it.active && it.storeId == currentStore.id },
                topStaff = topStaff,
                recentTransactions = commerce.transactions.filter { it.storeId == currentStore.id }.take(5),
            )
        }.mapNotNull { it }
    }

    private fun resolveOwner(session: AuthSession?): BusinessOwner = when (session?.user?.role) {
        StaffRole.OWNER -> runBlocking { database.ownerDao().getOwnerById(session.user.relatedEntityId)?.toDomain() }
        else -> null
    } ?: runBlocking { database.ownerDao().getOwner().toDomain() }
}

private data class DashboardCommerceBundle(
    val analytics: BusinessAnalytics,
    val programs: List<RewardProgram>,
    val campaigns: List<Campaign>,
    val transactions: List<Transaction>,
)

private data class DashboardStaffBundle(
    val staff: List<StaffMember>,
    val analytics: List<StaffAnalytics>,
)

private data class AnalyticsDateWindow(
    val start: LocalDate,
    val end: LocalDate,
)

private fun AnalyticsTimeRange.windowEndingOn(endDate: LocalDate): AnalyticsDateWindow =
    AnalyticsDateWindow(start = startDateFrom(endDate), end = endDate)

private fun AnalyticsTimeRange.previousWindowBefore(endDate: LocalDate): AnalyticsDateWindow {
    val currentStart = startDateFrom(endDate)
    val days = ChronoUnit.DAYS.between(currentStart, endDate) + 1
    val previousEnd = currentStart.minusDays(1)
    return AnalyticsDateWindow(start = previousEnd.minusDays(days - 1), end = previousEnd)
}

private fun List<Transaction>.inDateRange(start: LocalDate, end: LocalDate): List<Transaction> =
    filter { it.timestamp.toLocalDate() in start..end }

private fun List<Transaction>.averageAmount(): Double =
    takeIf { it.isNotEmpty() }?.sumOf(Transaction::amount)?.div(size) ?: 0.0

private fun List<Transaction>.redemptionRate(): Double =
    if (isEmpty()) 0.0 else count { it.pointsRedeemed > 0 }.toDouble() / size

private fun List<Customer>.retentionRate(): Double =
    if (isEmpty()) 0.0 else count { it.totalVisits >= 3 }.toDouble() / size

private fun List<Customer>.averageLifetimeValue(): Double =
    if (isEmpty()) 0.0 else sumOf(Customer::totalSpent) / size

private fun growthRate(current: Double, previous: Double): Double = when {
    current == 0.0 && previous == 0.0 -> 0.0
    previous == 0.0 -> 1.0
    else -> (current - previous) / previous
}

private fun buildPeriodRevenueTrend(transactions: List<Transaction>, range: AnalyticsTimeRange): List<AnalyticsPoint> = when (range) {
    WEEK -> buildDailyPoints(7) { date -> transactions.filter { it.timestamp.toLocalDate() == date }.sumOf(Transaction::amount).toFloat() }
    MONTH -> buildWeeklyPoints(5) { start, end -> transactions.filter { it.timestamp.toLocalDate() in start..end }.sumOf(Transaction::amount).toFloat() }
    QUARTER -> buildMonthlyPoints(3) { month -> transactions.filter { YearMonth.from(it.timestamp) == month }.sumOf(Transaction::amount).toFloat() }
    YEAR -> buildMonthlyPoints(12) { month -> transactions.filter { YearMonth.from(it.timestamp) == month }.sumOf(Transaction::amount).toFloat() }
}

private fun buildPeriodVisitTrend(transactions: List<Transaction>, range: AnalyticsTimeRange): List<AnalyticsPoint> = when (range) {
    WEEK -> buildDailyPoints(7) { date -> transactions.count { it.timestamp.toLocalDate() == date }.toFloat() }
    MONTH -> buildWeeklyPoints(5) { start, end -> transactions.count { it.timestamp.toLocalDate() in start..end }.toFloat() }
    QUARTER -> buildMonthlyPoints(3) { month -> transactions.count { YearMonth.from(it.timestamp) == month }.toFloat() }
    YEAR -> buildMonthlyPoints(12) { month -> transactions.count { YearMonth.from(it.timestamp) == month }.toFloat() }
}

private fun buildCustomerActivityTrend(customers: List<Customer>, transactions: List<Transaction>, range: AnalyticsTimeRange): List<AnalyticsPoint> = when (range) {
    WEEK -> buildDailyPoints(7) { date ->
        val newCustomers = customers.count { it.enrolledDate == date }
        val visits = transactions.count { it.timestamp.toLocalDate() == date }
        (newCustomers + visits).toFloat()
    }
    MONTH -> buildWeeklyPoints(5) { start, end ->
        val newCustomers = customers.count { it.enrolledDate in start..end }
        val visits = transactions.count { it.timestamp.toLocalDate() in start..end }
        (newCustomers + visits).toFloat()
    }
    QUARTER -> buildMonthlyPoints(3) { month ->
        val newCustomers = customers.count { YearMonth.from(it.enrolledDate) == month }
        val visits = transactions.count { YearMonth.from(it.timestamp) == month }
        (newCustomers + visits).toFloat()
    }
    YEAR -> buildMonthlyPoints(12) { month ->
        val newCustomers = customers.count { YearMonth.from(it.enrolledDate) == month }
        val visits = transactions.count { YearMonth.from(it.timestamp) == month }
        (newCustomers + visits).toFloat()
    }
}

private fun buildRetentionTrend(range: AnalyticsTimeRange, currentVisitors: Set<String>, previousVisitors: Set<String>): List<AnalyticsPoint> {
    val retained = currentVisitors.intersect(previousVisitors).size.toFloat()
    val churned = (previousVisitors - currentVisitors).size.toFloat()
    return when (range) {
        WEEK, MONTH -> listOf(
            AnalyticsPoint("Retained", retained),
            AnalyticsPoint("Churned", churned),
        )
        QUARTER, YEAR -> listOf(
            AnalyticsPoint("Retained", retained),
            AnalyticsPoint("Reactivated", (currentVisitors - previousVisitors).size.toFloat()),
            AnalyticsPoint("Churned", churned),
        )
    }
}

private fun buildTimeBucketTrend(transactions: List<Transaction>): List<AnalyticsPoint> {
    val buckets = listOf(
        "Morning" to 6..11,
        "Lunch" to 12..14,
        "Afternoon" to 15..17,
        "Evening" to 18..22,
    )
    return buckets.map { (label, hours) ->
        AnalyticsPoint(label, transactions.filter { it.timestamp.hour in hours }.sumOf(Transaction::amount).toFloat())
    }
}

private fun revenueSourceBreakdown(transactions: List<Transaction>): List<AnalyticsSegment> {
    val promotionRevenue = transactions.filter { it.metadata.contains("promotion", ignoreCase = true) || it.metadata.contains("campaign", ignoreCase = true) }
        .sumOf(Transaction::amount)
    val loyaltyRevenue = transactions.filter { it.pointsRedeemed > 0 || it.pointsEarned > 0 }
        .sumOf(Transaction::amount)
    val directRevenue = (transactions.sumOf(Transaction::amount) - promotionRevenue - loyaltyRevenue).coerceAtLeast(0.0)
    return listOf(
        AnalyticsSegment("Direct sales", directRevenue.roundToInt()),
        AnalyticsSegment("Promotion sales", promotionRevenue.roundToInt()),
        AnalyticsSegment("Loyalty sales", loyaltyRevenue.roundToInt()),
    ).filter { it.value > 0 }
}

private fun customerTierSegments(customers: List<Customer>): List<AnalyticsSegment> =
    customers.groupBy { it.loyaltyTier.displayName() }
        .map { (tier, items) -> AnalyticsSegment(tier, items.size) }
        .sortedByDescending(AnalyticsSegment::value)

private fun highValueThreshold(customers: List<Customer>): Double =
    customers.map(Customer::totalSpent).average().takeIf { !it.isNaN() && it > 0.0 } ?: 100.0

private fun topPromotionName(promotions: List<Campaign>, transactions: List<Transaction>): String =
    promotions.maxByOrNull { estimatePromotionUsage(it, transactions) }?.name.orEmpty()

private fun estimatePromotionUsage(promotion: Campaign, transactions: List<Transaction>): Int =
    transactions.count { it.metadata.contains(promotion.id) || it.metadata.contains(promotion.name, ignoreCase = true) }

private fun estimatePromotionRevenueImpact(promotion: Campaign, transactions: List<Transaction>): Double =
    transactions.filter { it.metadata.contains(promotion.id) || it.metadata.contains(promotion.name, ignoreCase = true) }
        .sumOf(Transaction::amount)

private fun rewardUsageBreakdown(transactions: List<Transaction>): List<AnalyticsSegment> = listOf(
    AnalyticsSegment("Points issued", transactions.sumOf(Transaction::pointsEarned)),
    AnalyticsSegment("Points redeemed", transactions.sumOf(Transaction::pointsRedeemed)),
    AnalyticsSegment("Reward redemptions", transactions.count { it.pointsRedeemed > 0 }),
    AnalyticsSegment("Check-ins", transactions.count()),
).filter { it.value > 0 }

private fun estimateProgramMemberCount(program: RewardProgram, transactions: List<Transaction>): Int = when (program.type) {
    LoyaltyProgramType.POINTS,
    LoyaltyProgramType.CASHBACK,
    LoyaltyProgramType.HYBRID,
    LoyaltyProgramType.TIER,
    LoyaltyProgramType.COUPON,
    LoyaltyProgramType.PURCHASE_FREQUENCY,
    LoyaltyProgramType.REFERRAL -> transactions.filter { it.pointsEarned > 0 || it.pointsRedeemed > 0 }.map(Transaction::customerId).distinct().size
    LoyaltyProgramType.DIGITAL_STAMP -> transactions.map(Transaction::customerId).distinct().size
}

private fun estimateProgramRedemptionRate(program: RewardProgram, transactions: List<Transaction>): Double = when (program.type) {
    LoyaltyProgramType.POINTS,
    LoyaltyProgramType.CASHBACK,
    LoyaltyProgramType.HYBRID,
    LoyaltyProgramType.TIER,
    LoyaltyProgramType.COUPON,
    LoyaltyProgramType.PURCHASE_FREQUENCY,
    LoyaltyProgramType.REFERRAL -> {
        val earned = transactions.sumOf(Transaction::pointsEarned)
        if (earned == 0) 0.0 else transactions.sumOf(Transaction::pointsRedeemed).toDouble() / earned
    }
    LoyaltyProgramType.DIGITAL_STAMP -> if (transactions.isEmpty()) 0.0 else transactions.count { it.pointsRedeemed > 0 }.toDouble() / transactions.size
}

private fun buildDailyPoints(days: Int, valueForDate: (LocalDate) -> Float): List<AnalyticsPoint> {
    val today = LocalDate.now()
    return (days - 1 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        AnalyticsPoint(date.dayOfWeek.name.take(3), valueForDate(date))
    }
}

private fun buildWeeklyPoints(weeks: Int, valueForRange: (LocalDate, LocalDate) -> Float): List<AnalyticsPoint> {
    val today = LocalDate.now()
    return (weeks - 1 downTo 0).map { offset ->
        val end = today.minusDays((offset * 7L))
        val start = end.minusDays(6)
        AnalyticsPoint("W${weeks - offset}", valueForRange(start, end))
    }
}

private fun buildMonthlyPoints(months: Int, valueForMonth: (YearMonth) -> Float): List<AnalyticsPoint> {
    val currentMonth = YearMonth.now()
    return (months - 1 downTo 0).map { offset ->
        val month = currentMonth.minusMonths(offset.toLong())
        AnalyticsPoint(month.month.name.take(3), valueForMonth(month))
    }
}

private fun LoyaltyTier.displayName(): String = when (this) {
    LoyaltyTier.BRONZE -> "Bronze"
    LoyaltyTier.SILVER -> "Silver"
    LoyaltyTier.GOLD -> "Gold"
    LoyaltyTier.VIP -> "VIP"
}

private fun String.humanize(): String = lowercase().replace('_', ' ').replaceFirstChar(Char::titlecase)

private fun Customer.displayName(): String = listOf(firstName, lastName).joinToString(" ").trim()
