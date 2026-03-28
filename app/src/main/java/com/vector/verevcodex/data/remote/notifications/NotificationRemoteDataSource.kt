package com.vector.verevcodex.data.remote.notifications

import com.vector.verevcodex.data.remote.api.notifications.VerevNotificationsApi
import com.vector.verevcodex.data.remote.api.notifications.RegisterPushDeviceRequestDto
import com.vector.verevcodex.data.remote.api.notifications.UnregisterPushDeviceRequestDto
import com.vector.verevcodex.data.remote.core.parseRemoteInstant
import com.vector.verevcodex.data.remote.core.remoteResult
import com.vector.verevcodex.data.remote.core.unwrap
import com.vector.verevcodex.domain.model.notifications.MerchantNotification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRemoteDataSource @Inject constructor(
    private val api: VerevNotificationsApi,
) {
    suspend fun list(cursor: String? = null, limit: Int = 50): Result<List<MerchantNotification>> = remoteResult {
        api.list(cursor = cursor, limit = limit).unwrap { page ->
            page.items.orEmpty().map { dto ->
                MerchantNotification(
                    id = dto.id.orEmpty(),
                    eventId = dto.eventId.orEmpty(),
                    notificationType = dto.notificationType.orEmpty(),
                    title = dto.title.orEmpty(),
                    body = dto.body.orEmpty(),
                    payload = dto.payload.orEmpty(),
                    isRead = dto.status.orEmpty().equals("READ", ignoreCase = true),
                    createdAt = parseRemoteInstant(dto.createdAt),
                    readAt = dto.readAt?.let(::parseRemoteInstant),
                )
            }
        }
    }

    suspend fun markRead(notificationId: String): Result<Unit> = remoteResult {
        api.markRead(notificationId).unwrap { Unit }
    }

    suspend fun markAllRead(): Result<Unit> = remoteResult {
        api.markAllRead().unwrap { Unit }
    }

    suspend fun registerPushDevice(
        deviceToken: String,
        platform: String,
        appVersion: String?,
        deviceModel: String?,
        locale: String?,
    ): Result<Unit> = remoteResult {
        api.registerPushDevice(
            RegisterPushDeviceRequestDto(
                deviceToken = deviceToken,
                platform = platform,
                appVersion = appVersion,
                deviceModel = deviceModel,
                locale = locale,
            ),
        ).unwrap { Unit }
    }

    suspend fun unregisterPushDevice(deviceToken: String): Result<Unit> = remoteResult {
        api.unregisterPushDevice(
            UnregisterPushDeviceRequestDto(deviceToken = deviceToken),
        ).unwrap { Unit }
    }
}
