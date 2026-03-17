package com.vector.verevcodex.data.remote.api.reports

import com.google.gson.annotations.SerializedName

data class ReportFiltersRequestDto(
    @SerializedName("storeId") val storeId: String?,
    @SerializedName("dateFrom") val dateFrom: String,
    @SerializedName("dateTo") val dateTo: String,
)

data class ReportExportRequestDto(
    @SerializedName("reportType") val reportType: String,
    @SerializedName("format") val format: String,
    @SerializedName("filters") val filters: ReportFiltersRequestDto,
    @SerializedName("scheduleId") val scheduleId: String? = null,
)

data class ReportAutoSettingsRequestDto(
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("frequency") val frequency: String,
    @SerializedName("format") val format: String,
    @SerializedName("includeAllStores") val includeAllStores: Boolean,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("scheduledTime") val scheduledTime: String,
    @SerializedName("scheduledWeekday") val scheduledWeekday: String,
    @SerializedName("scheduledMonthDay") val scheduledMonthDay: Int,
    @SerializedName("recipientEmails") val recipientEmails: List<String>,
    @SerializedName("includedSections") val includedSections: List<String>,
)

data class ReportFiltersViewDto(
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("dateFrom") val dateFrom: String? = null,
    @SerializedName("dateTo") val dateTo: String? = null,
)

data class ReportExportViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("requestedByUserId") val requestedByUserId: String? = null,
    @SerializedName("scheduleId") val scheduleId: String? = null,
    @SerializedName("reportType") val reportType: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("filters") val filters: ReportFiltersViewDto? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("contentType") val contentType: String? = null,
    @SerializedName("fileSize") val fileSize: Long? = null,
    @SerializedName("downloadUrl") val downloadUrl: String? = null,
    @SerializedName("signedUrlExpiresAt") val signedUrlExpiresAt: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("errorMessage") val errorMessage: String? = null,
)

data class ReportAutoSettingsViewDto(
    @SerializedName("enabled") val enabled: Boolean? = null,
    @SerializedName("frequency") val frequency: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("includeAllStores") val includeAllStores: Boolean? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("scheduledTime") val scheduledTime: String? = null,
    @SerializedName("scheduledWeekday") val scheduledWeekday: String? = null,
    @SerializedName("scheduledMonthDay") val scheduledMonthDay: Int? = null,
    @SerializedName("recipientEmails") val recipientEmails: List<String>? = null,
    @SerializedName("includedSections") val includedSections: List<String>? = null,
)
