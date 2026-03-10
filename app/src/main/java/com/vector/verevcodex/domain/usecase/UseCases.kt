package com.vector.verevcodex.domain.usecase

import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerDraft
import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.domain.model.ReportExport
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.repository.AnalyticsRepository
import com.vector.verevcodex.domain.repository.CustomerRepository
import com.vector.verevcodex.domain.repository.LoyaltyRepository
import com.vector.verevcodex.domain.repository.ReportRepository
import com.vector.verevcodex.domain.repository.StaffRepository
import com.vector.verevcodex.domain.repository.StoreRepository
import com.vector.verevcodex.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class ObserveDashboardUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(): Flow<DashboardSnapshot> = repository.observeDashboardSnapshot()
}

class ObserveStoresUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeStores()
}

class ObserveSelectedStoreUseCase(private val repository: StoreRepository) {
    operator fun invoke() = repository.observeSelectedStore()
}

class SelectStoreUseCase(private val repository: StoreRepository) {
    suspend operator fun invoke(storeId: String) = repository.selectStore(storeId)
}

class ObserveCustomersUseCase(private val repository: CustomerRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeCustomers(storeId)
}

class ObserveCustomerUseCase(private val repository: CustomerRepository) {
    operator fun invoke(customerId: String) = repository.observeCustomer(customerId)
}

class FindCustomerByNfcUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(nfcId: String): Customer? = repository.findByNfcId(nfcId)
}

class CreateCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(draft: CustomerDraft, storeId: String): Customer = repository.createCustomer(draft, storeId)
}

class QuickRegisterCustomerUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(firstName: String, phoneNumber: String, nfcId: String, storeId: String): Customer {
        return repository.registerQuickCustomer(firstName, phoneNumber, nfcId, storeId)
    }
}

class AdjustCustomerPointsUseCase(private val repository: CustomerRepository) {
    suspend operator fun invoke(customerId: String, delta: Int, reason: String) = repository.adjustPoints(customerId, delta, reason)
}

class ObserveProgramsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observePrograms(storeId)
}

class ObserveRewardsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeRewards(storeId)
}

class ObserveCampaignsUseCase(private val repository: LoyaltyRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeCampaigns(storeId)
}

class ObserveStaffUseCase(private val repository: StaffRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeStaff(storeId)
}

class AddStaffMembersUseCase(private val repository: StaffRepository) {
    suspend operator fun invoke(storeId: String, members: List<StaffOnboardingMember>) = repository.addStaffMembers(storeId, members)
}

class ObserveStaffAnalyticsUseCase(private val repository: StaffRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeStaffAnalytics(storeId)
}

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {
    operator fun invoke(storeId: String? = null) = repository.observeTransactions(storeId)
}

class RecordTransactionUseCase(private val repository: TransactionRepository) {
    suspend operator fun invoke(transaction: Transaction) = repository.recordTransaction(transaction)
}

class ExportReportUseCase(private val repository: ReportRepository) {
    suspend operator fun invoke(storeId: String?, format: String): ReportExport = repository.exportBusinessReport(storeId, format)
}
