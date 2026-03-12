package com.vector.verevcodex.domain.model.loyalty

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDateTime

data class PointsLedger(
    override val id: String,
    val customerId: String,
    val transactionId: String?,
    val delta: Int,
    val reason: String,
    val createdAt: LocalDateTime,
) : Identifiable
