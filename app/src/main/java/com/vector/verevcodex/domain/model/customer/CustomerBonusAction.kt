package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDateTime

data class CustomerBonusAction(
    override val id: String,
    val customerId: String,
    val storeId: String?,
    val type: CustomerBonusActionType,
    val title: String,
    val details: String,
    val createdAt: LocalDateTime,
) : Identifiable
