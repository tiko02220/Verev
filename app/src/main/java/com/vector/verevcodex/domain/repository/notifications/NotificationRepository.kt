package com.vector.verevcodex.domain.repository.notifications

import com.vector.verevcodex.domain.model.notifications.MerchantNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun observeMerchantNotifications(limit: Int = 50): Flow<List<MerchantNotification>>
    fun observeUnreadMerchantNotificationCount(limit: Int = 50): Flow<Int>
    suspend fun markMerchantNotificationRead(notificationId: String): Result<Unit>
    suspend fun markAllMerchantNotificationsRead(): Result<Unit>
}
