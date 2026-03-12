package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDateTime

data class CustomerActivity(
    override val id: String,
    val type: CustomerActivityType,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val amount: Double? = null,
    val pointsDelta: Int? = null,
    val transactionId: String? = null,
) : Identifiable
