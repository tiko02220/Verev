package com.vector.verevcodex.data.repository.reports

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportExport
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.repository.analytics.AnalyticsRepository
import com.vector.verevcodex.domain.repository.loyalty.LoyaltyRepository
import com.vector.verevcodex.domain.repository.reports.ReportRepository
import com.vector.verevcodex.domain.repository.staff.StaffRepository
import com.vector.verevcodex.domain.repository.store.StoreRepository
import com.vector.verevcodex.domain.repository.transactions.TransactionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

private val Context.reportDataStore by preferencesDataStore(name = "merchant_prefs")

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val storeRepository: StoreRepository,
    private val staffRepository: StaffRepository,
    private val loyaltyRepository: LoyaltyRepository,
    private val transactionRepository: TransactionRepository,
    @ApplicationContext context: Context,
) : ReportRepository {
    private val dataStore = context.reportDataStore
    private val reportsDir = context.filesDir.resolve("reports")

    override fun observeAutoReportSettings(): Flow<ReportAutoSettings> = dataStore.data.map { preferences ->
        ReportAutoSettings(
            enabled = preferences[ReportPreferenceKeys.enabled] ?: false,
            frequency = preferences[ReportPreferenceKeys.frequency]?.let(ReportAutoFrequency::valueOf) ?: ReportAutoFrequency.WEEKLY,
            format = preferences[ReportPreferenceKeys.format]?.let(ReportFormat::valueOf) ?: ReportFormat.DOCX,
            includeAllStores = preferences[ReportPreferenceKeys.includeAllStores] ?: false,
        )
    }

    override suspend fun saveAutoReportSettings(settings: ReportAutoSettings) {
        dataStore.edit { preferences ->
            preferences[ReportPreferenceKeys.enabled] = settings.enabled
            preferences[ReportPreferenceKeys.frequency] = settings.frequency.name
            preferences[ReportPreferenceKeys.format] = settings.format.name
            preferences[ReportPreferenceKeys.includeAllStores] = settings.includeAllStores
        }
    }

    override suspend fun exportBusinessReport(storeId: String?, format: ReportFormat): ReportExport {
        val generatedAt = LocalDateTime.now()
        val snapshot = buildSnapshot(storeId, generatedAt)
        val document = BusinessReportDocumentFactory.create(snapshot)
        val file = ReportFileWriter.write(
            outputDir = reportsDir,
            fileName = ReportFileMetadata.fileName(storeId, format, generatedAt),
            format = format,
            document = document,
        )
        return ReportExport(
            fileName = file.name,
            format = format,
            summary = document.summary,
            absolutePath = file.absolutePath,
            mimeType = ReportFileMetadata.mimeType(format),
            generatedAt = generatedAt,
        )
    }

    private suspend fun buildSnapshot(storeId: String?, generatedAt: LocalDateTime): BusinessReportSnapshot {
        val stores = storeRepository.observeStores().first()
        val resolvedStoreName = stores.firstOrNull { store -> store.id == storeId }?.name ?: ReportText.allStores
        return BusinessReportSnapshot(
            resolvedStoreName = resolvedStoreName,
            generatedAt = generatedAt,
            business = analyticsRepository.observeBusinessAnalytics(storeId).first(),
            customer = analyticsRepository.observeCustomerAnalytics(storeId).first(),
            revenue = analyticsRepository.observeRevenueAnalytics(storeId).first(),
            promotion = analyticsRepository.observePromotionAnalytics(storeId).first(),
            program = analyticsRepository.observeProgramAnalytics(storeId).first(),
            staffAnalytics = staffRepository.observeStaffAnalytics(storeId).first(),
            transactions = transactionRepository.observeTransactions(storeId).first().take(10),
            activePrograms = loyaltyRepository.observePrograms(storeId).first().count { it.active },
            activePromotions = loyaltyRepository.observeCampaigns(storeId).first().count { it.active },
        )
    }
}
