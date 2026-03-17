package com.vector.verevcodex.data.remote.api

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("meta") val meta: ApiMeta? = null,
    @SerializedName("error") val error: ApiError? = null,
)

data class ApiMeta(
    @SerializedName("requestId") val requestId: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
)

data class ApiError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("details") val details: Any? = null,
)
