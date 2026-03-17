package com.vector.verevcodex.data.remote.api.transactions

import com.google.gson.annotations.SerializedName

data class TransactionViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("staffUserId") val staffUserId: String? = null,
    @SerializedName("transactionType") val transactionType: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("currencyCode") val currencyCode: String? = null,
    @SerializedName("pointsEarned") val pointsEarned: Int? = null,
    @SerializedName("pointsRedeemed") val pointsRedeemed: Int? = null,
    @SerializedName("approvalRequestId") val approvalRequestId: String? = null,
    @SerializedName("originalTransactionId") val originalTransactionId: String? = null,
    @SerializedName("occurredAt") val occurredAt: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null,
)

data class TransactionListResponseDto(
    @SerializedName("items") val items: List<TransactionViewDto>? = null,
)

data class TransactionCommitRequestDto(
    @SerializedName("storeId") val storeId: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("redeemPoints") val redeemPoints: Int,
    @SerializedName("rewardId") val rewardId: String?,
    @SerializedName("occurredAt") val occurredAt: String,
    @SerializedName("items") val items: List<TransactionItemRequestDto>,
    @SerializedName("notes") val notes: String?,
)

data class TransactionItemRequestDto(
    @SerializedName("sku") val sku: String,
    @SerializedName("title") val title: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
)

data class TransactionCommitResponseDto(
    @SerializedName("transaction") val transaction: TransactionViewDto? = null,
)
