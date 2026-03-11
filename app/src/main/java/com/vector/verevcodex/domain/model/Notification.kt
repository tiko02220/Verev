package com.vector.verevcodex.domain.model

import java.time.LocalDateTime

data class Notification(
    override val id: String,
    val storeId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
) : Identifiable
