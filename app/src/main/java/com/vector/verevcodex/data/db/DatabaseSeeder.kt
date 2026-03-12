package com.vector.verevcodex.data.db

import com.vector.verevcodex.domain.model.business.Store
import javax.inject.Inject
import javax.inject.Singleton

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
        database.customerCredentialDao().insertAll(SeedData.customerCredentials)
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
        database.businessSettingsDao().upsertInvoices(SeedData.billingInvoices)
        SeedData.subscriptionPlans.forEach { database.businessSettingsDao().upsertSubscriptionPlan(it) }
        database.businessSettingsDao().upsertPaymentMethods(SeedData.paymentMethods)
        SeedData.brandingSettings.forEach { database.businessSettingsDao().upsertBrandingSettings(it) }
        SeedData.branchConfigurations.forEach { database.businessSettingsDao().upsertBranchConfiguration(it) }
    }
}
