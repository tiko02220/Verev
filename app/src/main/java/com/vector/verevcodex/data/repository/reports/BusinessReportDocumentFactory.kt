package com.vector.verevcodex.data.repository.reports

internal object BusinessReportDocumentFactory {
    fun create(snapshot: BusinessReportSnapshot): ReportDocument = ReportDocument(
        title = ReportText.reportTitle,
        subtitle = ReportFormatting.subtitle(snapshot.resolvedStoreName, snapshot.generatedAt),
        summary = ReportFormatting.summary(
            totalCustomers = snapshot.business.totalCustomers,
            totalRevenue = snapshot.revenue.totalRevenue,
            averageTicket = snapshot.business.averagePurchaseValue,
        ),
        sections = listOf(
            overviewSection(snapshot),
            revenueSection(snapshot),
            customerSection(snapshot),
            programsPromotionsSection(snapshot),
            staffSection(snapshot),
            recentTransactionsSection(snapshot),
        ),
    )

    private fun overviewSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.overviewTitle,
        rows = listOf(
            ReportText.totalCustomers to snapshot.business.totalCustomers.toString(),
            ReportText.newCustomers to snapshot.business.newCustomers.toString(),
            ReportText.visitsToday to snapshot.business.visitsToday.toString(),
            ReportText.averagePurchaseValue to ReportFormatting.currency(snapshot.business.averagePurchaseValue),
            ReportText.rewardRedemptionRate to ReportFormatting.percent(snapshot.business.rewardRedemptionRate),
            ReportText.retentionRate to ReportFormatting.percent(snapshot.business.retentionRate),
        ),
    )

    private fun revenueSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.revenueTitle,
        rows = listOf(
            ReportText.totalRevenue to ReportFormatting.currency(snapshot.revenue.totalRevenue),
            ReportText.todayRevenue to ReportFormatting.currency(snapshot.revenue.todayRevenue),
            ReportText.averageOrderValue to ReportFormatting.currency(snapshot.revenue.averageOrderValue),
            ReportText.transactions to snapshot.revenue.transactionCount.toString(),
            ReportText.redeemedPointsValue to ReportFormatting.currency(snapshot.revenue.redeemedPointsValue),
        ),
    )

    private fun customerSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.customersTitle,
        rows = listOf(
            ReportText.returningCustomers to snapshot.customer.returningCustomers.toString(),
            ReportText.retainedCustomers to snapshot.customer.retainedCustomers.toString(),
            ReportText.averageLifetimeValue to ReportFormatting.currency(snapshot.customer.averageLifetimeValue),
            ReportText.topCustomer to (snapshot.customer.topCustomers.firstOrNull()?.customerName ?: ReportText.noValue),
        ),
    )

    private fun programsPromotionsSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.programsPromotionsTitle,
        rows = listOf(
            ReportText.activePrograms to snapshot.activePrograms.toString(),
            ReportText.totalPrograms to snapshot.program.totalPrograms.toString(),
            ReportText.activePromotions to snapshot.activePromotions.toString(),
            ReportText.paymentPromotions to snapshot.promotion.paymentPromotions.toString(),
        ),
    )

    private fun staffSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.staffPerformanceTitle,
        rows = snapshot.staffAnalytics.take(5).map { staff ->
            "${staff.staffId.takeLast(4)} • ${staff.transactionsProcessed} tx" to
                "${ReportFormatting.currency(staff.revenueHandled)} revenue"
        }.ifEmpty {
            listOf(ReportText.staffPerformanceTitle to ReportText.noStaffRecords)
        },
    )

    private fun recentTransactionsSection(snapshot: BusinessReportSnapshot) = ReportSection(
        title = ReportText.recentTransactionsTitle,
        rows = snapshot.transactions.map { transaction ->
            ReportFormatting.transactionTimestamp(transaction.timestamp) to
                "${ReportFormatting.currency(transaction.amount)} • ${transaction.pointsEarned} pts"
        }.ifEmpty {
            listOf(ReportText.transactions to ReportText.noTransactions)
        },
    )
}
