package com.vector.verevcodex.data.remote.reports

import com.vector.verevcodex.data.remote.api.reports.ReportExportRequestDto
import com.vector.verevcodex.data.remote.api.reports.ReportAutoSettingsRequestDto
import com.vector.verevcodex.data.remote.api.reports.ReportAutoSettingsViewDto
import com.vector.verevcodex.data.remote.api.reports.ReportExportViewDto
import com.vector.verevcodex.data.remote.api.reports.ReportFiltersRequestDto
import com.vector.verevcodex.data.remote.api.reports.VerevReportsApi
import com.vector.verevcodex.data.remote.auth.ApiException
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyAction
import com.vector.verevcodex.data.remote.core.RemoteIdempotencyDomain
import com.vector.verevcodex.data.remote.core.buildRemoteIdempotencyKey
import com.vector.verevcodex.data.remote.core.parseRemoteInstant
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.reports.ReportAutoFrequency
import com.vector.verevcodex.domain.model.reports.ReportAutoSettings
import com.vector.verevcodex.domain.model.reports.ReportFormat
import com.vector.verevcodex.domain.model.reports.ReportSection
import com.vector.verevcodex.domain.model.reports.ReportWeekday
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

data class RemoteReportDownload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val generatedAt: LocalDateTime,
)

@Singleton
class ReportsRemoteDataSource @Inject constructor(
    private val api: VerevReportsApi,
    private val backendBaseUrl: String,
) {
    suspend fun getAutoSettings(): Result<ReportAutoSettings> = runCatching {
        api.autoSettings().unwrap { it.toDomain() }
    }

    suspend fun saveAutoSettings(settings: ReportAutoSettings, selectedStoreId: String?): Result<ReportAutoSettings> = runCatching {
        api.upsertAutoSettings(
            request = settings.toRequestDto(selectedStoreId),
            idempotencyKey = reportIdempotencyKey(
                action = RemoteIdempotencyAction.UPDATE,
                settings.enabled.toString(),
                settings.frequency.name,
                settings.format.name,
                settings.includeAllStores.toString(),
                selectedStoreId,
                settings.scheduledTime,
                settings.scheduledWeekday.name,
                settings.scheduledMonthDay.toString(),
                settings.recipientEmails.joinToString("|"),
                settings.includedSections.joinToString("|") { it.name },
            ),
        ).unwrap { it.toDomain() }
    }

    suspend fun exportBusinessSummary(storeId: String?, format: ReportFormat): Result<RemoteReportDownload> = runCatching {
        val now = LocalDate.now()
        val initial = api.requestExport(
            ReportExportRequestDto(
                reportType = "BUSINESS_SUMMARY",
                format = format.name,
                filters = ReportFiltersRequestDto(
                    storeId = storeId,
                    dateFrom = now.minusDays(29).toString(),
                    dateTo = now.toString(),
                ),
            ),
            idempotencyKey = reportIdempotencyKey(
                action = RemoteIdempotencyAction.EXPORT,
                UUID.randomUUID().toString(),
                storeId,
                format.name,
                now.minusDays(29).toString(),
                now.toString(),
            ),
        ).unwrap { it }

        val completed = waitForCompletedExport(initial.id.orEmpty())
        val downloadUrl = completed.downloadUrl ?: throw ApiException(500, "Report download URL is missing")
        val response = api.download(resolveUrl(downloadUrl))
        if (!response.isSuccessful) {
            throw ApiException(response.code(), "Report download failed")
        }
        val bytes = response.body()?.bytes() ?: throw ApiException(response.code(), "Empty report download")
        RemoteReportDownload(
            fileName = completed.fileName.orEmpty().ifBlank { defaultFileName(format) },
            mimeType = completed.contentType.orEmpty().ifBlank { defaultMimeType(format) },
            bytes = bytes,
            generatedAt = LocalDateTime.ofInstant(
                parseRemoteInstant(completed.completedAt ?: Instant.now().toString()),
                ZoneId.systemDefault(),
            ),
        )
    }

    private suspend fun waitForCompletedExport(exportId: String): ReportExportViewDto {
        repeat(25) { attempt ->
            val export = api.getExport(exportId).unwrap { it }
            when (export.status.orEmpty().uppercase()) {
                "COMPLETED" -> return export
                "FAILED", "EXPIRED" -> {
                    val msg = export.errorMessage?.takeIf { it.isNotBlank() }
                        ?: "Backend report export ${export.status.orEmpty().lowercase()}"
                    throw ApiException(500, msg)
                }
            }
            delay(if (attempt < 5) 1000L else 1500L)
        }
        throw ApiException(504, "Timed out waiting for backend report export")
    }

    private fun resolveUrl(downloadUrl: String): String =
        if (downloadUrl.startsWith("http://") || downloadUrl.startsWith("https://")) {
            downloadUrl
        } else {
            backendBaseUrl.trimEnd('/') + "/" + downloadUrl.trimStart('/')
        }

    private fun defaultFileName(format: ReportFormat): String =
        "business-summary.${when (format) {
            ReportFormat.DOCX -> "docx"
            ReportFormat.XLSX -> "xlsx"
            ReportFormat.PDF -> "pdf"
        }}"

    private fun defaultMimeType(format: ReportFormat): String =
        when (format) {
            ReportFormat.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ReportFormat.XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ReportFormat.PDF -> "application/pdf"
        }

    private fun reportIdempotencyKey(
        action: RemoteIdempotencyAction,
        vararg parts: String?,
    ): String = buildRemoteIdempotencyKey(RemoteIdempotencyDomain.REPORT, action, *parts)
}

private fun ReportAutoSettings.toRequestDto(selectedStoreId: String?) = ReportAutoSettingsRequestDto(
    enabled = enabled,
    frequency = frequency.name,
    format = format.name,
    includeAllStores = includeAllStores,
    storeId = if (includeAllStores) null else selectedStoreId,
    scheduledTime = scheduledTime,
    scheduledWeekday = scheduledWeekday.name,
    scheduledMonthDay = scheduledMonthDay,
    recipientEmails = recipientEmails.toList(),
    includedSections = includedSections.map { it.name },
)

private fun ReportAutoSettingsViewDto.toDomain() = ReportAutoSettings(
    enabled = enabled ?: false,
    frequency = runCatching { ReportAutoFrequency.valueOf(frequency.orEmpty()) }.getOrElse { ReportAutoFrequency.WEEKLY },
    format = runCatching { ReportFormat.valueOf(format.orEmpty()) }.getOrElse { ReportFormat.DOCX },
    includeAllStores = includeAllStores ?: true,
    scheduledTime = scheduledTime.orEmpty().ifBlank { "09:00" },
    scheduledWeekday = runCatching { ReportWeekday.valueOf(scheduledWeekday.orEmpty()) }.getOrElse { ReportWeekday.MONDAY },
    scheduledMonthDay = (scheduledMonthDay ?: 1).coerceIn(1, 28),
    recipientEmails = recipientEmails.orEmpty().map { it.trim() }.filter { it.isNotBlank() }.toSet(),
    includedSections = includedSections.orEmpty()
        .mapNotNull { value -> runCatching { ReportSection.valueOf(value) }.getOrNull() }
        .toSet()
        .ifEmpty { ReportSection.entries.toSet() },
)
