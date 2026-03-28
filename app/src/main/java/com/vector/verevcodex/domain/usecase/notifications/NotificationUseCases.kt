package com.vector.verevcodex.domain.usecase.notifications

import com.vector.verevcodex.domain.repository.notifications.NotificationRepository
import javax.inject.Inject

class ObserveMerchantNotificationsUseCase @Inject constructor(private val repository: NotificationRepository) {
    operator fun invoke(limit: Int = 50) = repository.observeMerchantNotifications(limit)
}

class ObserveUnreadMerchantNotificationCountUseCase @Inject constructor(private val repository: NotificationRepository) {
    operator fun invoke(limit: Int = 50) = repository.observeUnreadMerchantNotificationCount(limit)
}

class MarkMerchantNotificationReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke(notificationId: String) = repository.markMerchantNotificationRead(notificationId)
}

class MarkAllMerchantNotificationsReadUseCase @Inject constructor(private val repository: NotificationRepository) {
    suspend operator fun invoke() = repository.markAllMerchantNotificationsRead()
}
