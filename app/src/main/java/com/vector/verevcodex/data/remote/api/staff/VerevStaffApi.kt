package com.vector.verevcodex.data.remote.api.staff

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VerevStaffApi {
    @GET("v1/staff")
    suspend fun list(): Response<ApiEnvelope<List<StaffViewDto>>>

    @POST("v1/staff/bulk")
    suspend fun bulkCreate(
        @Body request: BulkCreateStaffRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String,
    ): Response<ApiEnvelope<List<StaffViewDto>>>

    @GET("v1/staff/{staffId}")
    suspend fun get(@Path("staffId") staffId: String): Response<ApiEnvelope<StaffViewDto>>

    @PUT("v1/staff/{staffId}")
    suspend fun update(
        @Path("staffId") staffId: String,
        @Body request: UpdateStaffRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String,
    ): Response<ApiEnvelope<StaffViewDto>>

    @DELETE("v1/staff/{staffId}")
    suspend fun delete(
        @Path("staffId") staffId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String,
    ): Response<ApiEnvelope<StaffViewDto>>
}
