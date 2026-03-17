package com.vector.verevcodex.data.remote.api.reports

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Url

interface VerevReportsApi {
    @GET("v1/reports/auto-settings")
    suspend fun autoSettings(): Response<ApiEnvelope<ReportAutoSettingsViewDto>>

    @PUT("v1/reports/auto-settings")
    suspend fun upsertAutoSettings(
        @Body request: ReportAutoSettingsRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<ReportAutoSettingsViewDto>>

    @POST("v1/reports/exports")
    suspend fun requestExport(
        @Body request: ReportExportRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<ReportExportViewDto>>

    @GET("v1/reports/exports/{exportId}")
    suspend fun getExport(@Path("exportId") exportId: String): Response<ApiEnvelope<ReportExportViewDto>>

    @GET
    suspend fun download(@Url url: String): Response<ResponseBody>
}
