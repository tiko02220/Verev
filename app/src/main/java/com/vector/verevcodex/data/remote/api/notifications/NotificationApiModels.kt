package com.vector.verevcodex.data.remote.api.notifications

import com.google.gson.annotations.SerializedName

data class MerchantNotificationViewDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("eventId") val eventId: String? = null,
    @SerializedName("notificationType") val notificationType: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("payload") val payload: Map<String, Any?>? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("readAt") val readAt: String? = null,
)

data class NotificationPageDto(
    @SerializedName("items") val items: List<MerchantNotificationViewDto>? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null,
)

data class RegisterPushDeviceRequestDto(
    @SerializedName("deviceToken") val deviceToken: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("appVersion") val appVersion: String? = null,
    @SerializedName("deviceModel") val deviceModel: String? = null,
    @SerializedName("locale") val locale: String? = null,
)

data class UnregisterPushDeviceRequestDto(
    @SerializedName("deviceToken") val deviceToken: String,
)
