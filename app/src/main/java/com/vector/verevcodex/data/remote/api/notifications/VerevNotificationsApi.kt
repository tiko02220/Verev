package com.vector.verevcodex.data.remote.api.notifications

import com.vector.verevcodex.data.remote.api.ApiEnvelope
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VerevNotificationsApi {
    @GET("v1/notifications")
    suspend fun list(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): Response<ApiEnvelope<NotificationPageDto>>

    @POST("v1/notifications/{notificationId}/read")
    suspend fun markRead(
        @Path("notificationId") notificationId: String,
    ): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/notifications/read-all")
    suspend fun markAllRead(): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/notifications/devices")
    suspend fun registerPushDevice(
        @Body request: RegisterPushDeviceRequestDto,
    ): Response<ApiEnvelope<Map<String, Boolean>>>

    @POST("v1/notifications/devices/unregister")
    suspend fun unregisterPushDevice(
        @Body request: UnregisterPushDeviceRequestDto,
    ): Response<ApiEnvelope<Map<String, Boolean>>>
}
