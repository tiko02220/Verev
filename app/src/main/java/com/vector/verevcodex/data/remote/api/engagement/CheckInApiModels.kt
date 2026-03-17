package com.vector.verevcodex.data.remote.api.engagement

import com.google.gson.annotations.SerializedName

data class CheckInRequestDto(
    @SerializedName("storeId") val storeId: String,
    @SerializedName("customerId") val customerId: String,
    @SerializedName("occurredAt") val occurredAt: String?,
)

data class CheckInResponseDto(
    @SerializedName("checkInId") val checkInId: String? = null,
    @SerializedName("transactionId") val transactionId: String? = null,
    @SerializedName("customerId") val customerId: String? = null,
    @SerializedName("storeId") val storeId: String? = null,
    @SerializedName("deduplicated") val deduplicated: Boolean? = null,
    @SerializedName("rewardIssued") val rewardIssued: Boolean? = null,
    @SerializedName("rewardPoints") val rewardPoints: Int? = null,
    @SerializedName("rewardTitle") val rewardTitle: String? = null,
    @SerializedName("successfulCheckIns") val successfulCheckIns: Int? = null,
    @SerializedName("organizationPoints") val organizationPoints: Int? = null,
    @SerializedName("networkPoints") val networkPoints: Int? = null,
    @SerializedName("nextEligibleAt") val nextEligibleAt: String? = null,
)
