package com.vector.verevcodex.data.repository.reports

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.vector.verevcodex.data.remote.reports.ReportsRemoteDataSource
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportExport
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.reports.ReportSection
import com.vector.verevcodex.domain.model.reports.ReportWeekday
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.map

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val storeRepository: StoreRepository,
    private val staffRepository: StaffRepository,
    private val loyaltyRepository: LoyaltyRepository,
    private val transactionRepository: TransactionRepository,
    private val reportsRemote: ReportsRemoteDataSource,
    @ApplicationContext private val context: Context,
) : ReportRepository {
    private val reportsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?.resolve("VerevReports")
        ?: context.filesDir.resolve("reports")
    private val reportSettingsRefreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun observeAutoReportSettings(): Flow<ReportAutoSettings> =
        reportSettingsRefreshRequests
            .onStart { emit(Unit) }
            .map { reportsRemote.getAutoSettings().getOrThrow() }

    override suspend fun saveAutoReportSettings(settings: ReportAutoSettings) {
        val selectedStoreId = if (settings.includeAllStores) {
            null
        } else {
            storeRepository.observeSelectedStore().first()?.id
                ?: throw IllegalStateException("A store must be selected to save scoped auto report settings")
        }
        reportsRemote.saveAutoSettings(settings, selectedStoreId).getOrThrow()
        reportSettingsRefreshRequests.tryEmit(Unit)
    }

    override suspend fun exportBusinessReport(storeId: String?, format: ReportFormat): ReportExport {
        val remote = reportsRemote.exportBusinessSummary(storeId, format).getOrThrow()
        reportsDir.mkdirs()
        val file = reportsDir.resolve(remote.fileName)
        file.writeBytes(remote.bytes)

        var contentUri: String? = null
        var storageLocation: String? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, remote.fileName)
                put(MediaStore.Downloads.MIME_TYPE, remote.mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/VerevReports")
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { it.write(remote.bytes) }
                contentUri = uri.toString()
                storageLocation = "Downloads/VerevReports"
            }
        }
        if (storageLocation == null && reportsDir.absolutePath.contains("Download")) {
            storageLocation = "Download/VerevReports"
        }
        return ReportExport(
            fileName = file.name,
            format = format,
            summary = buildString {
                append("Business growth report ready in ")
                append(format.name)
                append(". Includes KPI highlights, revenue trend, customer value signals, loyalty activity, and recommended actions.")
            },
            absolutePath = file.absolutePath,
            mimeType = remote.mimeType,
            generatedAt = remote.generatedAt,
            storageLocation = storageLocation,
            contentUri = contentUri,
        )
    }
}
