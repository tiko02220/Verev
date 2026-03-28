package com.vector.verevcodex.data.remote.api.transactions

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VerevTransactionsApi {

    @GET("v1/transactions")
    suspend fun list(): Response<ApiEnvelope<TransactionListResponseDto>>

    @GET("v1/transactions/{transactionId}")
    suspend fun get(@Path("transactionId") transactionId: String): Response<ApiEnvelope<TransactionViewDto>>

    @GET("v1/approvals")
    suspend fun listApprovals(
        @Query("status") status: String? = null,
    ): Response<ApiEnvelope<List<ApprovalRequestDto>>>

    @POST("v1/transactions")
    suspend fun commit(
        @Body request: TransactionCommitRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<TransactionCommitResponseDto>>

    @POST("v1/transactions/{transactionId}/void-requests")
    suspend fun requestVoid(
        @Path("transactionId") transactionId: String,
        @Body request: VoidTransactionRequestDto,
    ): Response<ApiEnvelope<ApprovalRequestDto>>
}
