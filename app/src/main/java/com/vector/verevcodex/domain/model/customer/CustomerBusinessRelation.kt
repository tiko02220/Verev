package com.vector.verevcodex.domain.model.customer

import com.vector.verevcodex.domain.model.common.Identifiable
import java.time.LocalDateTime

data class CustomerBusinessRelation(
    override val id: String,
    val customerId: String,
    val storeId: String,
    val joinedAt: LocalDateTime,
    val notes: String,
    val tags: List<String> = emptyList(),
) : Identifiable
