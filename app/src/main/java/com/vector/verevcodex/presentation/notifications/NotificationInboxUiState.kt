package com.vector.verevcodex.presentation.notifications

import com.vector.verevcodex.domain.model.notifications.MerchantNotification

data class NotificationInboxUiState(
    val isLoading: Boolean = true,
    val notifications: List<MerchantNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isMarkingAllRead: Boolean = false,
)
