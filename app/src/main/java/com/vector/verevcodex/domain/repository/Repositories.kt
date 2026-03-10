package com.vector.verevcodex.domain.repository

import com.vector.verevcodex.domain.model.BusinessAnalytics
import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerDraft
import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.domain.model.ReportExport
import com.vector.verevcodex.domain.model.Reward
import com.vector.verevcodex.domain.model.RewardProgram
import com.vector.verevcodex.domain.model.StaffAnalytics
import com.vector.verevcodex.domain.model.StaffMember
import com.vector.verevcodex.domain.model.Store
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeStores(): Flow<List<Store>>
    fun observeSelectedStore(): Flow<Store?>
    suspend fun selectStore(storeId: String)
}

interface StaffRepository {
    fun observeStaff(storeId: String? = null): Flow<List<StaffMember>>
    fun observeStaffAnalytics(storeId: String? = null): Flow<List<StaffAnalytics>>
    suspend fun addStaffMembers(storeId: String, members: List<StaffOnboardingMember>): Result<Unit>
}

interface CustomerRepository {
    fun observeCustomers(storeId: String? = null): Flow<List<Customer>>
    fun observeCustomer(customerId: String): Flow<Customer?>
    suspend fun findByNfcId(nfcId: String): Customer?
    suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer
    suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, nfcId: String, storeId: String): Customer
    suspend fun adjustPoints(customerId: String, delta: Int, reason: String)
}

interface LoyaltyRepository {
    fun observePrograms(storeId: String? = null): Flow<List<RewardProgram>>
    fun observeRewards(storeId: String? = null): Flow<List<Reward>>
    fun observeCampaigns(storeId: String? = null): Flow<List<Campaign>>
}

interface TransactionRepository {
    fun observeTransactions(storeId: String? = null): Flow<List<Transaction>>
    suspend fun recordTransaction(transaction: Transaction)
}

interface AnalyticsRepository {
    fun observeBusinessAnalytics(storeId: String? = null): Flow<BusinessAnalytics>
    fun observeDashboardSnapshot(): Flow<DashboardSnapshot>
}

interface ReportRepository {
    suspend fun exportBusinessReport(storeId: String?, format: String): ReportExport
}
