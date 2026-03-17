package com.vector.verevcodex.data.remote.api.billing

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VerevBillingApi {

    @GET("v1/billing/plans")
    suspend fun plans(): Response<ApiEnvelope<List<PlanCatalogViewDto>>>

    @GET("v1/billing/overview")
    suspend fun overview(): Response<ApiEnvelope<BillingOverviewResponseDto>>

    @GET("v1/billing/payment-methods")
    suspend fun paymentMethods(): Response<ApiEnvelope<List<PaymentMethodViewDto>>>

    @POST("v1/billing/payment-methods")
    suspend fun createPaymentMethod(
        @Body request: CreatePaymentMethodRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<PaymentMethodViewDto>>

    @POST("v1/billing/payment-methods/{paymentMethodId}/default")
    suspend fun setDefaultPaymentMethod(
        @Path("paymentMethodId") paymentMethodId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<PaymentMethodViewDto>>

    @DELETE("v1/billing/payment-methods/{paymentMethodId}")
    suspend fun removePaymentMethod(
        @Path("paymentMethodId") paymentMethodId: String,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<RemovePaymentMethodResponseDto>>

    @PUT("v1/billing/subscription")
    suspend fun updateCurrentSubscription(
        @Body request: UpdateCurrentSubscriptionRequestDto,
        @Header("X-Idempotency-Key") idempotencyKey: String? = null,
    ): Response<ApiEnvelope<OrganizationSubscriptionViewDto>>

    @GET("v1/billing/invoices")
    suspend fun invoices(): Response<ApiEnvelope<List<BillingInvoiceViewDto>>>
}
