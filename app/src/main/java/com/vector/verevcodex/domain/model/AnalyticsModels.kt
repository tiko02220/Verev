package com.vector.verevcodex.domain.model

data class BusinessAnalytics(
    override val id: String,
    val scopeStoreId: String?,
    val totalCustomers: Int,
    val newCustomers: Int,
    val visitsToday: Int,
    val averagePurchaseValue: Double,
    val rewardRedemptionRate: Double,
    val retentionRate: Double,
    val topCustomerName: String,
) : Identifiable

data class StaffAnalytics(
    override val id: String,
    val staffId: String,
    val storeId: String,
    val transactionsProcessed: Int,
    val revenueHandled: Double,
    val customersServed: Int,
    val rewardsRedeemed: Int,
    val averageTransactionValue: Double,
) : Identifiable

data class ReportExport(
    val fileName: String,
    val format: String,
    val summary: String,
)

data class DashboardSnapshot(
    val owner: BusinessOwner,
    val selectedStore: Store,
    val stores: List<Store>,
    val analytics: BusinessAnalytics,
    val activePrograms: List<RewardProgram>,
    val activeCampaigns: List<Campaign>,
    val topStaff: List<Pair<StaffMember, StaffAnalytics>>,
    val recentTransactions: List<Transaction>,
)
