package com.vector.verevcodex.domain.model.notifications

import com.vector.verevcodex.domain.model.common.Identifiable
import com.vector.verevcodex.domain.model.common.NotificationType
import java.time.LocalDateTime

data class Notification(
    override val id: String,
    val storeId: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val createdAt: LocalDateTime,
) : Identifiable
