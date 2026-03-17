package com.vector.verevcodex.data.remote.api.transactions

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface VerevTransactionsApi {

    @GET("v1/transactions")
    suspend fun list(): Response<ApiEnvelope<TransactionListResponseDto>>

    @GET("v1/transactions/{transactionId}")
    suspend fun get(@Path("transactionId") transactionId: String): Response<ApiEnvelope<TransactionViewDto>>

    @POST("v1/transactions")
    suspend fun commit(
        @Body request: TransactionCommitRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<TransactionCommitResponseDto>>
}
