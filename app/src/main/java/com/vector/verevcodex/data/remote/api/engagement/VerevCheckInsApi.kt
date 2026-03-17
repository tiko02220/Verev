package com.vector.verevcodex.data.remote.api.engagement

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface VerevCheckInsApi {
    @POST("v1/check-ins")
    suspend fun create(
        @Body request: CheckInRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<CheckInResponseDto>>
}
