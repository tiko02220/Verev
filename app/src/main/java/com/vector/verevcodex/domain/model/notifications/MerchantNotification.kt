package com.vector.verevcodex.domain.model.notifications

import java.time.Instant

data class MerchantNotification(
    val id: String,
    val eventId: String,
    val notificationType: String,
    val title: String,
    val body: String,
    val payload: Map<String, Any?>,
    val isRead: Boolean,
    val createdAt: Instant,
    val readAt: Instant?,
)
