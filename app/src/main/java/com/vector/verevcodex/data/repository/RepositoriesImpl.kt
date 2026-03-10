package com.vector.verevcodex.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vector.verevcodex.data.db.AppDatabase
import com.vector.verevcodex.data.db.SeedData
import com.vector.verevcodex.data.db.entity.CustomerBusinessRelationEntity
import com.vector.verevcodex.data.db.entity.CustomerEntity
import com.vector.verevcodex.data.db.entity.PointsLedgerEntity
import com.vector.verevcodex.data.db.entity.StaffMemberEntity
import com.vector.verevcodex.data.mapper.toDomain
import com.vector.verevcodex.data.mapper.toEntity
import com.vector.verevcodex.domain.model.BusinessAnalytics
import com.vector.verevcodex.domain.model.Campaign
import com.vector.verevcodex.domain.model.Customer
import com.vector.verevcodex.domain.model.CustomerDraft
import com.vector.verevcodex.domain.model.DashboardSnapshot
import com.vector.verevcodex.domain.model.LoyaltyTier
import com.vector.verevcodex.domain.model.ReportExport
import com.vector.verevcodex.domain.model.StaffAnalytics
import com.vector.verevcodex.domain.model.Store
import com.vector.verevcodex.domain.model.Transaction
import com.vector.verevcodex.domain.model.auth.StaffOnboardingMember
import com.vector.verevcodex.domain.repository.AnalyticsRepository
import com.vector.verevcodex.domain.repository.CustomerRepository
import com.vector.verevcodex.domain.repository.LoyaltyRepository
import com.vector.verevcodex.domain.repository.ReportRepository
import com.vector.verevcodex.domain.repository.StaffRepository
import com.vector.verevcodex.domain.repository.StoreRepository
import com.vector.verevcodex.domain.repository.TransactionRepository
import com.vector.verevcodex.domain.repository.auth.AuthRepository
import com.vector.verevcodex.domain.model.StaffRole
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val Context.dataStore by preferencesDataStore(name = "merchant_prefs")

@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: AppDatabase,
) {
    suspend fun seedIfNeeded() {
        if (database.storeDao().getStore(SeedData.stores.first().id) != null) return
        database.ownerDao().insertAll(listOf(SeedData.owner))
        database.storeDao().insertAll(SeedData.stores)
        database.staffDao().insertAll(SeedData.staff)
        database.customerDao().insertAll(SeedData.customers)
        database.customerBusinessRelationDao().insertAll(SeedData.customerRelations)
        database.transactionDao().insertAll(SeedData.transactions)
        database.transactionItemDao().insertAll(SeedData.transactionItems)
        database.loyaltyDao().insertPrograms(SeedData.programs)
        database.loyaltyDao().insertRewards(SeedData.rewards)
        database.loyaltyDao().insertCampaigns(SeedData.campaigns)
        database.loyaltyDao().insertCampaignTargets(SeedData.campaignTargets)
        database.pointsLedgerDao().insertAll(SeedData.ledger)
        database.notificationDao().insertAll(SeedData.notifications)
        database.authDao().insertAll(SeedData.authAccounts)
    }
}

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
    seeder: DatabaseSeeder,
) : StoreRepository {
    private val dataStore = context.dataStore

    init {
        runBlocking { seeder.seedIfNeeded() }
    }

    override fun observeStores(): Flow<List<Store>> = combine(
        database.storeDao().observeStores(),
        authRepository.observeSession(),
    ) { entities, session ->
        val filtered = when {
            session == null -> entities
            session.user.role == StaffRole.OWNER -> entities.filter { entity -> entity.ownerId == session.user.relatedEntityId }
            else -> {
                val staffStoreId = database.staffDao().getById(session.user.relatedEntityId)?.storeId
                entities.filter { entity -> entity.id == staffStoreId }
            }
        }
        filtered.map { it.toDomain() }
    }

    override fun observeSelectedStore(): Flow<Store?> = combine(
        observeStores(),
        authRepository.observeSession(),
        dataStore.data,
    ) { stores, session, preferences ->
        val selectedStoreKey = stringPreferencesKey("${session?.user?.id ?: "anonymous"}_selected_store_id")
        val selectedId = preferences[selectedStoreKey]
        stores.firstOrNull { it.id == selectedId } ?: stores.firstOrNull()
    }

    override suspend fun selectStore(storeId: String) {
        val sessionId = authRepository.observeSession().first()?.user?.id ?: "anonymous"
        val selectedStoreKey = stringPreferencesKey("${sessionId}_selected_store_id")
        dataStore.edit { prefs -> prefs[selectedStoreKey] = storeId }
    }
}

@Singleton
class CustomerRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : CustomerRepository {
    override fun observeCustomers(storeId: String?): Flow<List<Customer>> =
        database.customerDao().observeCustomers(storeId).map { list -> list.distinctBy { it.id }.map { it.toDomain() } }

    override fun observeCustomer(customerId: String): Flow<Customer?> =
        database.customerDao().observeCustomer(customerId).map { it?.toDomain() }

    override suspend fun findByNfcId(nfcId: String): Customer? = database.customerDao().findByNfcId(nfcId)?.toDomain()

    override suspend fun createCustomer(draft: CustomerDraft, storeId: String): Customer {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            phoneNumber = draft.phoneNumber.trim(),
            email = draft.email.trim().lowercase(),
            nfcId = draft.nfcId?.trim()?.ifBlank { null } ?: "MANUAL-${UUID.randomUUID().toString().take(8)}",
            enrolledDate = LocalDate.now().toString(),
            totalVisits = 0,
            totalSpent = 0.0,
            currentPoints = 0,
            loyaltyTier = LoyaltyTier.BRONZE.name,
            lastVisit = null,
            favoriteStoreId = storeId,
        )
        database.customerDao().insert(customer)
        database.customerBusinessRelationDao().insert(
            CustomerBusinessRelationEntity(
                id = UUID.randomUUID().toString(),
                customerId = customer.id,
                storeId = storeId,
                joinedAt = LocalDateTime.now().toString(),
                notes = "Manual registration from dashboard flow",
            )
        )
        return customer.toDomain()
    }

    override suspend fun registerQuickCustomer(firstName: String, phoneNumber: String, nfcId: String, storeId: String): Customer {
        return createCustomer(
            draft = CustomerDraft(
                firstName = firstName,
                lastName = "",
                phoneNumber = phoneNumber,
                email = "",
                nfcId = nfcId,
            ),
            storeId = storeId,
        )
    }

    override suspend fun adjustPoints(customerId: String, delta: Int, reason: String) {
        val customer = database.customerDao().getCustomer(customerId) ?: return
        database.customerDao().update(customer.copy(currentPoints = customer.currentPoints + delta))
        database.pointsLedgerDao().insert(
            PointsLedgerEntity(UUID.randomUUID().toString(), customerId, null, delta, reason, LocalDateTime.now().toString())
        )
    }
}

@Singleton
class LoyaltyRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : LoyaltyRepository {
    override fun observePrograms(storeId: String?) = database.loyaltyDao().observePrograms(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeRewards(storeId: String?) = database.loyaltyDao().observeRewards(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeCampaigns(storeId: String?): Flow<List<Campaign>> = combine(
        database.loyaltyDao().observeCampaigns(storeId),
        MutableStateFlow(runBlocking { database.loyaltyDao().getCampaignTargets() }),
    ) { campaigns, targets ->
        campaigns.mapNotNull { campaign -> targets.firstOrNull { it.campaignId == campaign.id }?.let { campaign.toDomain(it) } }
    }
}

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : TransactionRepository {
    override fun observeTransactions(storeId: String?): Flow<List<Transaction>> =
        database.transactionDao().observeTransactions(storeId).map { transactions ->
            val items = database.transactionItemDao().getByTransactionIds(transactions.map { it.id })
            transactions.map { tx -> tx.toDomain(items.filter { it.transactionId == tx.id }) }
        }

    override suspend fun recordTransaction(transaction: Transaction) {
        database.transactionDao().insert(transaction.toEntity())
        database.transactionItemDao().insertAll(transaction.items.map { it.toEntity() })
        val customer = database.customerDao().getCustomer(transaction.customerId) ?: return
        val updatedCustomer = customer.copy(
            totalVisits = customer.totalVisits + 1,
            totalSpent = customer.totalSpent + transaction.amount,
            currentPoints = customer.currentPoints + transaction.pointsEarned - transaction.pointsRedeemed,
            lastVisit = transaction.timestamp.toString(),
            favoriteStoreId = transaction.storeId,
        )
        database.customerDao().update(updatedCustomer)
        database.pointsLedgerDao().insert(
            PointsLedgerEntity(UUID.randomUUID().toString(), transaction.customerId, transaction.id, transaction.pointsEarned - transaction.pointsRedeemed, "Transaction sync", transaction.timestamp.toString())
        )
    }
}

@Singleton
class StaffRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
) : StaffRepository {
    override fun observeStaff(storeId: String?) = database.staffDao().observeStaff(storeId).map { list -> list.map { it.toDomain() } }

    override fun observeStaffAnalytics(storeId: String?): Flow<List<StaffAnalytics>> = combine(
        observeStaff(storeId),
        database.transactionDao().observeTransactions(storeId),
    ) { staff, transactions ->
        staff.map { member ->
            val handled = transactions.filter { it.staffId == member.id }
            val revenue = handled.sumOf { it.amount }
            StaffAnalytics(
                id = member.id,
                staffId = member.id,
                storeId = member.storeId,
                transactionsProcessed = handled.size,
                revenueHandled = revenue,
                customersServed = handled.map { it.customerId }.distinct().size,
                rewardsRedeemed = handled.count { it.pointsRedeemed > 0 },
                averageTransactionValue = if (handled.isEmpty()) 0.0 else revenue / handled.size,
            )
        }
    }

    override suspend fun addStaffMembers(storeId: String, members: List<StaffOnboardingMember>): Result<Unit> {
        members.forEach { member ->
            val staffId = UUID.randomUUID().toString()
            val authId = UUID.randomUUID().toString()
            val nameParts = member.fullName.trim().split(" ", limit = 2)
            val firstName = nameParts.firstOrNull().orEmpty()
            val lastName = nameParts.getOrElse(1) { "" }

            database.staffDao().insertAll(
                listOf(
                    StaffMemberEntity(
                        id = staffId,
                        storeId = storeId,
                        firstName = firstName,
                        lastName = lastName,
                        email = member.email.trim().lowercase(),
                        phoneNumber = "",
                        role = member.role.name,
                        active = true,
                        permissionsSummary = member.permissionsSummary,
                    )
                )
            )
            database.authDao().insert(
                com.vector.verevcodex.data.db.entity.auth.AuthAccountEntity(
                    id = authId,
                    relatedEntityId = staffId,
                    fullName = member.fullName.trim(),
                    email = member.email.trim().lowercase(),
                    phoneNumber = "",
                    password = member.password,
                    role = member.role.name,
                    active = true,
                )
            )
        }
        return Result.success(Unit)
    }
}

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
    override fun observeBusinessAnalytics(storeId: String?): Flow<BusinessAnalytics> = combine(
        database.customerDao().observeCustomers(storeId),
        database.transactionDao().observeTransactions(storeId),
    ) { customers, transactions ->
        val today = LocalDate.now()
        val newCustomers = customers.count { LocalDate.parse(it.enrolledDate) >= today.minusDays(30) }
        val visitsToday = transactions.count { LocalDateTime.parse(it.timestamp).toLocalDate() == today }
        val totalRevenue = transactions.sumOf { it.amount }
        BusinessAnalytics(
            id = storeId ?: "all-stores",
            scopeStoreId = storeId,
            totalCustomers = customers.distinctBy { it.id }.size,
            newCustomers = newCustomers,
            visitsToday = visitsToday,
            averagePurchaseValue = if (transactions.isEmpty()) 0.0 else totalRevenue / transactions.size,
            rewardRedemptionRate = if (transactions.isEmpty()) 0.0 else transactions.count { it.pointsRedeemed > 0 }.toDouble() / transactions.size,
            retentionRate = customers.count { it.totalVisits >= 3 }.toDouble() / customers.size.coerceAtLeast(1),
            topCustomerName = customers.maxByOrNull { it.totalSpent }?.let { "${it.firstName} ${it.lastName}".trim() }.orEmpty(),
        )
    }

    override fun observeDashboardSnapshot(): Flow<DashboardSnapshot> {
        val storeSelectionFlow: Flow<Pair<Store?, List<Store>>> = combine(
            storeRepository.observeSelectedStore(),
            storeRepository.observeStores(),
        ) { selectedStore: Store?, stores: List<Store> ->
            selectedStore to stores
        }

        val commerceFlow = storeRepository.observeSelectedStore()
            .map { selectedStore -> selectedStore?.id }
            .flatMapLatest { selectedStoreId ->
            combine(
                observeBusinessAnalytics(selectedStoreId),
                loyaltyRepository.observePrograms(selectedStoreId),
                loyaltyRepository.observeCampaigns(selectedStoreId),
                transactionRepository.observeTransactions(selectedStoreId),
            ) { analytics, programs, campaigns, transactions ->
                DashboardCommerceBundle(analytics, programs, campaigns, transactions)
            }
        }

        val staffFlow = storeRepository.observeSelectedStore()
            .map { selectedStore -> selectedStore?.id }
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
            val owner = when (session?.user?.role) {
                StaffRole.OWNER -> database.ownerDao().getOwnerById(session.user.relatedEntityId)?.toDomain()
                else -> null
            } ?: runBlocking { database.ownerDao().getOwner().toDomain() }
            val topStaff = staffBundle.analytics
                .sortedByDescending { analyticsItem -> analyticsItem.revenueHandled }
                .take(3)
                .mapNotNull { analyticsItem ->
                    staffBundle.staff.firstOrNull { staffMember -> staffMember.id == analyticsItem.staffId }
                        ?.let { staffMember -> staffMember to analyticsItem }
                }

            DashboardSnapshot(
                owner = owner,
                selectedStore = currentStore,
                stores = stores,
                analytics = commerce.analytics.copy(scopeStoreId = currentStore.id),
                activePrograms = commerce.programs.filter { program -> program.active && program.storeId == currentStore.id },
                activeCampaigns = commerce.campaigns.filter { campaign -> campaign.active && campaign.storeId == currentStore.id },
                topStaff = topStaff,
                recentTransactions = commerce.transactions.filter { transaction -> transaction.storeId == currentStore.id }.take(5),
            )
        }.mapNotNull { it }
    }
}

private data class DashboardCommerceBundle(
    val analytics: BusinessAnalytics,
    val programs: List<com.vector.verevcodex.domain.model.RewardProgram>,
    val campaigns: List<Campaign>,
    val transactions: List<Transaction>,
)

private data class DashboardStaffBundle(
    val staff: List<com.vector.verevcodex.domain.model.StaffMember>,
    val analytics: List<StaffAnalytics>,
)

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
) : ReportRepository {
    override suspend fun exportBusinessReport(storeId: String?, format: String): ReportExport {
        val analytics = analyticsRepository.observeBusinessAnalytics(storeId).first()
        val storePart = storeId ?: "all-stores"
        return ReportExport(
            fileName = "verev-report-$storePart.${format.lowercase()}",
            format = format,
            summary = "Customers ${analytics.totalCustomers}, visits today ${analytics.visitsToday}, avg ticket ${"%.2f".format(analytics.averagePurchaseValue)}",
        )
    }
}
